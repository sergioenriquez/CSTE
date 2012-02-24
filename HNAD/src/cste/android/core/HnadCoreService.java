package cste.android.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.DeviceDetailsActivity;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.LoginActivity;
import cste.hnad.CsdMessageHandler;
import cste.hnad.Device;
import cste.hnad.HnadCoreInterface;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.messages.IcdMsg;
import cste.misc.ZigbeeAPI;
import cste.misc.ZigbeeFrame;

public class HnadCoreService extends Service implements HnadCoreInterface{
	private static final String TAG = "HNAD Core Service";
	//Test ECOC device does not recognize secure HNAD type? Will use FNAD for now...
	public DeviceType thisDevType 	= DeviceType.FNAD_I; //HNAD_S
	public DeviceUID thisUID 		= new DeviceUID("0013A20040715FD8");
	public DeviceType dcpDevType 	= DeviceType.DCP;
	public DeviceUID dcpUID 		= new DeviceUID("0807060504030211");
	public DeviceType lvl2DevType 	= DeviceType.INVALID;
	public DeviceUID lvl2UID 		= new DeviceUID("0000000000000000");
	
	
	List<DeviceUID> mMsgWaitingList;
	private Hashtable<String,Device> mDeviceMap;
	
	private boolean mIsLoggedIn = false;
	private NADABroadcaster mNadaBroadcaster;
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommManager mUsbCommHandler;
	private NetworkHandler mNetworkHandler;
	private NotificationManager mNM;
	private Handler mNadaHandler = new Handler();
	
	private final IBinder mBinder = new LocalBinder();
	private Timer mUsbReconnectTimer;

	/*****************************/

	public Hashtable<String,Device> getDeviceList(){
		return mDeviceMap;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		NetworkHandler.setServiceHost(this);
		
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommManager(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		mUsbReconnectTimer = new Timer("USB reconnect timer",true);
		mDeviceMap = new Hashtable<String,Device>(10);
		mDeviceMap.put("keu", new Device(true,"keu","ECOC"));

		mMsgWaitingList = new ArrayList<DeviceUID>();
		mMsgWaitingList.add(new DeviceUID("FFFFFFFFFFFFFFFF") );
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        
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
		
		//assume log in was successful, connect USB device
		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.LOGIN_RESULT,true);
		sendBroadcast(intent);
		
		mUsbReconnectTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if ( mUsbCommHandler.openExistingUSBaccessory() )
					this.cancel();
			}}, 100, 1000);// delay start 100ms, retry every second
	}

	@Override
	public void uploadData() {
		// TODO Auto-generated method stub	

	}

    @Override
    public void onUsbStateChanged(boolean connected){
    	if(connected)
    	{
    		//toast("USB is connected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		mNadaHandler.post(mNadaBroadcaster);
    		
    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.USB_STATE_CHANGED,true);
    		sendBroadcast(intent);
    	}
    	else
    	{
    		//toast("USB was disconnected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		
    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.USB_STATE_CHANGED,false);
    		sendBroadcast(intent);
    	}
    }
    
    @Override
	public void onPacketReceived(ZigbeeFrame frm) {
    	if( frm.type == ZigbeeAPI.RX_64BIT)
    	{
    		IcdMsg msg = IcdMsg.fromBytes(frm.payload);
    		if( msg.getStatus() == IcdMsg.MsgStatus.OK)
    			handleIcdMsg(msg);
    		else
    			Log.w(TAG,"Received ICD msg with an error");
    	}
	}
    
    /***
     * 
     * @param msg
     */
    private void handleIcdMsg(IcdMsg msg)
    {
    	String key = msg.getHeader().getDevUID().toString();
    	if( mDeviceMap.containsKey( key ) )
    	{
        	//toast("pkt from UID " + key);
        	
    	}
    	else
    	{
    		mDeviceMap.put(key, new Device(
    				true,
    				key,
    				msg.getHeader().getDevType().toString()));
    		
    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.DEVLIST_CHANGED,key);
    		sendBroadcast(intent);
    		
    		showNewDeviceNotification( key);
    	}
    }
    
    /***************************/
    
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NEW_DEVICE_NOTIFICATION);
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
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
    
	private void showNewDeviceNotification(String key) {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		//TODO go to the device details for this new device
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + key;        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_wifi_signal_0, text, System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		Intent intent = new Intent(this, DeviceDetailsActivity.class);
		intent.putExtra("deviceKey", key); // TODO fill actual device
		intent.putExtra("clearNotifications",true); // TODO fill actual device
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification. 
		notification.vibrate = new long[]{Notification.DEFAULT_VIBRATE};
		mNM.notify(NEW_DEVICE_NOTIFICATION, notification);    
	}

	public class Events{
		public static final String HNAD_CORE_EVENT_MSG = "cste.hnad.android.HNAD_CORE_EVENT";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
		public static final String USB_STATE_CHANGED = "USB CONNECTED";
	}
	
	public static final int NEW_DEVICE_NOTIFICATION = 0;
}
//startService(new Intent(this,UsbService.class));