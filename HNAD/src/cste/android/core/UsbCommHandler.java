package cste.android.core;

//TODO handle notification event on USB disconnected
//TODO handle USB already being connected on app launch


import static cste.hnad.CsdMessageHandler.DEVICE_CONNECTED;
import static cste.hnad.CsdMessageHandler.DEVICE_DISCONNECTED;
import static cste.hnad.CsdMessageHandler.PACKET_RECEIVED;

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
public class UsbCommHandler extends BroadcastReceiver{
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
	
	//private UsbWriter mUsbWriter;
	//private UsbReader mUsbReader;
	
	private ArrayBlockingQueue<byte[]> pendingTxList;
	
	/**
	 * Constructor needs a reference to the running HNAD core service, and the message handler used to pass data to it
	 * @param hostService
	 */
	public UsbCommHandler(Service hostService, Handler messageHandler){
		mHandler = messageHandler;
		mHostService = hostService;
		pendingTxList = new ArrayBlockingQueue<byte[]>(50);
		mUsbManager = UsbManager.getInstance(hostService);
		
		mPermissionIntent = PendingIntent.getBroadcast(hostService, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		hostService.registerReceiver(this, filter);
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
	public void openExistingUSBaccessory(){
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		mAccessory = (accessories == null ? null : accessories[0]);
		if (mAccessory != null) {
			if (mUsbManager.hasPermission(mAccessory)) {
				openAccessory(mAccessory);
			} else {
				synchronized (this) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(mAccessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}

	/***
	 * Opens a connection to the specified accessory
	 * @param accessory
	 */
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);

			Thread thread1 = new Thread(null, new UsbWriter(), "Usb Writer thred");
			thread1.start();
			
			Thread thread2 = new Thread(null, new UsbReader(), "Usb Reader thred");
			thread2.start();
			
			Log.d(TAG, "accessory opened");
			Message.obtain(mHandler,DEVICE_CONNECTED).sendToTarget();
		} else {
			Log.d(TAG, "accessory open fail");
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
					Log.e(TAG,e.getMessage());
					break;
				}
				
				try {
					mOutputStream.write(msg);
				} catch (IOException e) {
					Log.e(TAG,e.getMessage());
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
					Message m = Message.obtain(mHandler,PACKET_RECEIVED);
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
		if( mAccessory != null){
			if ( pendingTxList.size() < 50)
			{
				pendingTxList.add(message);
				Log.w(TAG, "USB Tx queue is full, msg discarded");
				return true;
			}
		}
		
		return false;
	}

	/***
	 * 
	 */
	public void deRegister(){
		mHostService.unregisterReceiver(this);
	}
	
}//end class
