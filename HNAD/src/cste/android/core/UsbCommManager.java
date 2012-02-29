package cste.android.core;

import static cste.hnad.CsdMessageHandler.DEVICE_CONNECTED;
import static cste.hnad.CsdMessageHandler.DEVICE_DISCONNECTED;
import static cste.hnad.CsdMessageHandler.MSG_RECEIVED;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
/***
 * Provides an interface to transmit and receive from the arduino USB host device connected to the phone
 * @author Sergio Enriquez
 *
 */
public class UsbCommManager extends BroadcastReceiver{
	private static final String TAG = "USB Comm Handler";
	private static final String ACTION_USB_PERMISSION = "cste.android.usb.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;
	private Handler mHandler;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	private Service mHostService;
	private boolean isConnected = false;
	
	private ArrayBlockingQueue<byte[]> pendingTxList;
	
	/**
	 * Constructor needs a reference to the running HNAD core service, and the message handler used to pass data to it
	 * @param hostService
	 */
	public UsbCommManager(Service hostService, Handler messageHandler){
		mHandler = messageHandler;
		mHostService = hostService;
		pendingTxList = new ArrayBlockingQueue<byte[]>(50);
		mUsbManager = UsbManager.getInstance(hostService);
		
		mPermissionIntent = PendingIntent.getBroadcast(hostService, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		filter.addAction(UsbManager.EXTRA_PERMISSION_GRANTED);
		hostService.registerReceiver(this, filter);
	}
	
	boolean testUSB()
	{
		try {
			mOutputStream.write(0);
		} catch (IOException e) {
			mUsbManager = UsbManager.getInstance(mHostService);
			return false;
		}
		return true;
	}
	
	@Override
	/***
	 * Called when the BroadcastReceiver has received an intent
	 */
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		UsbAccessory accessory;
		if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			accessory = UsbManager.getAccessory(intent);
			if (accessory != null && accessory.equals(mAccessory)) {
				closeAccessory();
				
			}
		}else if (ACTION_USB_PERMISSION.equals(action)) {
			synchronized (this) {
				accessory = UsbManager.getAccessory(intent);
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					openAccessory(accessory);
					
				} else {
					Log.d(TAG, "permission denied for accessory " + accessory);
				}
				mPermissionRequestPending = false;
			}
		}
	}
	
	/***
	 * Query the USB manager to find if there is an attached accessory, and open it
	 * 
	 */
	public boolean openExistingUSBaccessory(){

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		mAccessory = (accessories == null ? null : accessories[0]);
		if (mAccessory != null) {
			if (mUsbManager.hasPermission(mAccessory)) {
				return openAccessory(mAccessory);
			} else {
				synchronized (this) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(mAccessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
				Log.w(TAG,"No permission to open the USB device");
				return false; // no permission, this should not happen normally 
			}
		} else {
			Log.d(TAG, "USB device is not connected");
			return false;
		}
	}

	/***
	 * Opens a connection to the specified accessory
	 * @param accessory
	 */
	private boolean openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			
			if ( !testUSB() )
			{
				isConnected = false;
				closeAccessory();
				return false;
			}
			
			isConnected = true;
			Thread thread1 = new Thread(null, new UsbWriter(), "Usb Writer thread");
			thread1.start();
			
			Thread thread2 = new Thread(null, new UsbReader(), "Usb Reader thread");
			thread2.start();
			
			Log.d(TAG, "accessory opened");
			Message.obtain(mHandler,DEVICE_CONNECTED).sendToTarget();
			
			return true;
		} else {
			Log.d(TAG, "accessory open fail");
			return false;
		}
	}
	
	/***
	 * Thread 
	 * @author user
	 *
	 */
	private class UsbWriter implements Runnable{
		@Override
		public void run() {
			byte []msg;
			while(mAccessory != null)
			{
				try {
					msg = pendingTxList.take();
				} catch (InterruptedException e) {
					Log.e(TAG,"Thread interrupted");
					break;
				}
				
				try {
					mOutputStream.write(msg);
				} catch (IOException e) {
					Log.e(TAG,"I/O error");
					break;
				}
			}
			closeAccessory();
		}
	}
	
	private class UsbReader implements Runnable{
		@Override
		public void run() {
			int ret = 0;
			byte[] buffer = new byte[16384];
			while (mAccessory != null && ret >= 0) {
				try {
					ret = mInputStream.read(buffer);
				} catch (IOException e) {
					Log.e(TAG,e.getMessage());
					closeAccessory();
					break;
				}
				
				if(ret>0)
				{
					Message m = Message.obtain(mHandler,MSG_RECEIVED);
					Bundle data = new Bundle();
					data.putByteArray("content", buffer);
					m.setData(data);
					mHandler.sendMessage(m);
				}
			}
		}//end run
	}//end class

	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
				if( isConnected )
					Message.obtain(mHandler,DEVICE_DISCONNECTED).sendToTarget();
			}
		} catch (IOException e) {
			Log.e(TAG,e.getMessage());
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
	
	/***
	 * 
	 * @param message
	 * @return
	 */
	boolean transmit(byte[] message)
	{
		if( message == null || message.length == 0)
			return false;
		
		if( mAccessory != null){
			if ( pendingTxList.size() < 50)
			{
				pendingTxList.add(message);
				return true;
			}
			else
				Log.w(TAG, "USB Tx queue is full, msg discarded");
		}
		
		return false;
	}

	/***
	 * 
	 */
	public void deRegister(){
		closeAccessory();
		mHostService.unregisterReceiver(this);
	}
	
}//end class
