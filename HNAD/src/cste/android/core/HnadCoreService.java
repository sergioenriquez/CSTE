package cste.android.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.LoginActivity;
import cste.hnad.CsdMessageHandler;
import cste.hnad.Device;
import cste.hnad.HnadCoreInterface;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.messages.IcdMsg;
import cste.messages.NADA;
import cste.misc.*;

import static cste.hnad.CsdMessageHandler.PACKET_RECEIVED;

public class HnadCoreService extends Service implements HnadCoreInterface{
	
	//Test ECOC device does not recognize secure HNAD type? Will use FNAD for now...
	public DeviceType thisDevType 	= DeviceType.FNAD_I; //HNAD_S
	public DeviceUID thisUID 		= new DeviceUID("0013A20040715FD8");
	public DeviceType dcpDevType 	= DeviceType.DCP;
	public DeviceUID dcpUID 		= new DeviceUID("0807060504030211");
	public DeviceType lvl2DevType 	= DeviceType.INVALID;
	public DeviceUID lvl2UID 		= new DeviceUID("0000000000000000");
	
	
	List<DeviceUID> mMsgWaitingList;
	private List<Device> mDeviceList;
	private boolean mIsLoggedIn = false;
	
	private NADABroadcaster mNadaBroadcaster;
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommHandler mUsbCommHandler;
	private NetworkHandler mNetworkHandler;
	private NotificationManager mNM;
	private Handler mNadaHandler = new Handler();
	private final int NOTIFICATION = 0;

	/*****************************/

	public List<Device> getDeviceList(){
		return mDeviceList;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		NetworkHandler.setServiceHost(this);
		
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommHandler(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		
		mDeviceList = new ArrayList<Device>();
		mDeviceList.add(new Device(true,"aaaa","yyy"));
		mDeviceList.add(new Device(true,"bbbb","yzxcy"));
		mDeviceList.add(new Device(true,"bbbb","asdas"));
		
		mMsgWaitingList = new ArrayList<DeviceUID>();
		mMsgWaitingList.add(new DeviceUID("FFFFFFFFFFFFFFFF") );
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
    	mUsbCommHandler.openExistingUSBaccessory();
        
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
        // We want this service to continue running until it is explicitly stopped, so return sticky.
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

    @Override
    public void onUsbStateChanged(boolean connected){
    	if(connected)
    	{
    		toast("USB was connected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		mNadaHandler.post(mNadaBroadcaster);
    	}
    	else
    	{
    		toast("USB was disconnected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    	}
    }
    
    @Override
	public void onPacketReceived(ZigbeePkt pkt) {
    	String src = toHex(pkt.sourceAddrs);
    	toast("pkt from " + src);

	}
    
    static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%1$02X", b));
        }

        return sb.toString();
    }


    
    /***************************/
    
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        mUsbCommHandler.deRegister();
    }

    private void toast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
    
	private void showNotification() {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		CharSequence title = "HNAD app";
		CharSequence text = "Click here to launch app";        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_wifi_signal_0, text, System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DeviceListActivity.class), 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification.        
		mNM.notify(NOTIFICATION, notification);    
	}

	public class Events{
		public static final String HNAD_CORE_EVENT_MSG = "cste.hnad.android.HNAD_CORE_EVENT";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
	}

	
}
//startService(new Intent(this,UsbService.class));