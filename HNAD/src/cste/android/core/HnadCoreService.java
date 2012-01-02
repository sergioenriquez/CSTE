package cste.android.core;

import java.util.ArrayList;
import java.util.List;

import com.android.future.usb.UsbAccessory;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.LoginActivity;
import cste.hnad.CsdMessageHandler;
import cste.hnad.Device;
import cste.hnad.HnadCoreInterface;
import cste.messages.IcdMsg;

public class HnadCoreService extends Service implements HnadCoreInterface{
	
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommHandler mUsbCommHandler;
	private NetworkHandler mNetworkHandler;

	private List<Device> mDeviceList;
	private NotificationManager mNM;
	
	private int NOTIFICATION = 0;
	
	private boolean mIsLoggedIn = false;

	public List<Device> getDeviceList(){
		return mDeviceList;
	}
	
	public class LocalBinder extends Binder {        
		public HnadCoreService getService() {        
			
			return HnadCoreService.this;        
		}    
	}
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		NetworkHandler.setServiceHost(this);
		//mNetworkHandler = new NetworkHandler();
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommHandler(this,mCsdMessageHandler);
		
		mDeviceList = new ArrayList<Device>();
		mDeviceList.add(new Device(true,"aaaa","yyy"));
		mDeviceList.add(new Device(true,"bbbb","yzxcy"));
		mDeviceList.add(new Device(true,"bbbb","asdas"));

		//showNotification();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        Bundle b = intent.getExtras();
        if ( b != null && b.containsKey("usbAccesory") )
        	mUsbCommHandler.openUsbAccesory();
        
        if ( mIsLoggedIn){
			Intent devListIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
			devListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(devListIntent);
		}else{
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            mIsLoggedIn = true;
		}

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	@Override
	public void login(String username, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadData() {
		// TODO Auto-generated method stub
		
	}

	private void showNotification() {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		CharSequence title = "HNAD app";
		CharSequence text = "Click here to launch app";        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_wifi_signal_0, text,                System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DeviceListActivity.class), 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification.        
		mNM.notify(NOTIFICATION, notification);    
	}

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        mUsbCommHandler.deRegister();
        // Tell the user we stopped.
        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

	@Override
	/***
	 * 
	 */
	public void packetReceived(IcdMsg msg) {
		// TODO Auto-generated method stub
		
		// do something with the content
		// decrypt if necesary
		
		int itemChanged = 0;
		
		
//		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG);
//		intent.putExtra(Events.DEVLIST_CHANGED,msg);
//		sendBroadcast(intent);

		//add a device to the list
	}
	
	public class Events{
		public static final String HNAD_CORE_EVENT_MSG = "cste.hnad.android.HNAD_CORE_EVENT";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
	}


}
//startService(new Intent(this,UsbService.class));