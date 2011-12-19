package cste.android.hnad;

import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.usb.UsbCommHandler;
import cste.hnad.CsdMessageHandler;
import cste.hnad.Device;
import cste.hnad.HnadCoreInterface;

public class HnadCoreService extends Service implements HnadCoreInterface{
	
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommHandler mUsbCommHandler;
	private List<Device> mDeviceList;
	private NotificationManager mNM;
	
	private int NOTIFICATION = 0;
	
	private boolean mIsLoggedIn = false;
	
	public UsbCommHandler getUsbHandler(){
		return mUsbCommHandler;
	}
	
	public static final String DEVICES_UPDATED = "cste.hnad.android.EVENT_DEVICES_UPDATED";

	public class LocalBinder extends Binder {        
		HnadCoreService getService() {            
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
		
		// TODO Auto-generated method stub
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommHandler(this,mCsdMessageHandler);

		if ( mIsLoggedIn){
			Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
		}else{
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            mIsLoggedIn = true;
		}
		
		//showNotification();
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        mUsbCommHandler.deRegister();
        // Tell the user we stopped.
        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }



	public static final int DEVICE_ADDED = 1;
	public static final int DEVICE_REMOVED = 2;
	public static final int DEVICE_CHANGED = 3;

	@Override
	/***
	 * 
	 */
	public void packetReceived(int content) {
		// TODO Auto-generated method stub
		
		int itemChanged = 0;
		
		
		Intent intent = new Intent(DEVICES_UPDATED);
		intent.putExtra("itemChanged",content);
		sendBroadcast(intent);

		//add a device to the list
	}

}
//startService(new Intent(this,UsbService.class));