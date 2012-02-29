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
import cste.android.activities.HnadConfigurationTabActivity;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.LoginActivity;
import cste.hnad.CsdMessageHandler;
import cste.hnad.Device;
import cste.hnad.HNADServiceInterface;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;
import cste.icd.EcocCmdType;
import cste.icd.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import cste.messages.IcdMsg;
import cste.misc.ZigbeeAPI;
import cste.misc.ZigbeeFrame;
import static cste.icd.Utility.strToHex;

/***
 * 
 * @author Sergio Enriquez
 *
 */
public class HNADService extends Service implements HNADServiceInterface, KeyProvider{
	private static final String TAG = "HNAD Core Service";
	//Test ECOC device does not recognize secure HNAD type? Will use FNAD for now...
	public DeviceType thisDevType 	= DeviceType.FNAD_I; //HNAD_S
	public DeviceUID thisUID 		= new DeviceUID("0013A20040715FD8");//F354B5448C619A6C
	public DeviceType dcpDevType 	= DeviceType.INVALID;
	public DeviceUID dcpUID 		= new DeviceUID("0000000000000000");
	public DeviceType lvl2DevType 	= DeviceType.HNAD_S;
	public DeviceUID lvl2UID 		= new DeviceUID("0013A20040715FD8");
	public final byte icdRev 		= 0x02;//0x02
	
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
		
		IcdMsg.configure(thisDevType, thisUID, icdRev, this);
		
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommManager(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		mUsbReconnectTimer = new Timer("USB reconnect timer",true);
		mDeviceMap = new Hashtable<String,Device>(10);
		
		//mDeviceMap.put("0013A20040715FD8", new Device(new DeviceUID("0013A20040715FD8"),DeviceType.CSD));
		//mDeviceMap.put("0013A20040760BB4", new Device(new DeviceUID("0013A20040760BB4"),DeviceType.ACSD));
		//mDeviceMap.put("0013A200406BE0C3", new Device(new DeviceUID("0013A200406BE0C3"),DeviceType.ECOC));

		mMsgWaitingList = new ArrayList<DeviceUID>();
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
	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		byte[] key = new byte[16];
		//TODO use database to retrieve keys 1234567890ABCDEF1234567890ABCDEF
		return strToHex("1234567890ABCDEF1234567890ABCDEF"); //1234567890ABCDEF1234567890ABCDEF
	}				   //678780B177F9748B3D4CCB670A6938F1
	
	@Override
	public void getDeviceStatus(Device dev) {
		dev.incTxAsc();
		IcdMsg msg = IcdMsg.create(dev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SL);
		//IcdMsg msg = IcdMsg.create(dev, MsgType.DEV_CMD_UNRESTRICTED, UnrestrictedCmdType.REQUEST);
		byte [] zigbeeFrame = ZigbeeAPI.buildPkt(dev.UID().getBytes(),(byte)0x01,msg.getBytes());
		this.mUsbCommHandler.transmit(zigbeeFrame);
	}

    @Override
    public void onUsbStateChanged(boolean connected){
    	if(connected)
    	{
    		toast("USB is connected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		mNadaHandler.post(mNadaBroadcaster);
    		
    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.USB_STATE_CHANGED,true);
    		sendBroadcast(intent);
    	}
    	else
    	{
    		toast("USB was disconnected");
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
    	}else if( frm.type == ZigbeeAPI.TX_STATUS ){
    		if( frm.statusCode != 0)
    			toast("NO ACK");
    		else
    			toast("ACK");
    	}
	}
    
    /***
     * 
     * @param msg
     */
    private void handleIcdMsg(IcdMsg msg)
    {
    	String key = msg.header().getDevUID().toString();
    	Device device;
    	if( mDeviceMap.containsKey( key ) )
    	{
    		device = mDeviceMap.get(key);
    	}else
    	{
    		device = new Device(
    				msg.header().getDevUID(),
    				msg.header().getDevType());
    		device.setTxAsc(60);
    		
    		
    		mDeviceMap.put(key, device);
    		
    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.DEVLIST_CHANGED,key);
    		sendBroadcast(intent);
    		
    		showNewDeviceNotification(device);
    	}
    	
    	Intent testIntent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.DEVICE_INFO_CHANGED,false);
		testIntent.putExtra("Device",device);
		sendBroadcast(testIntent);
    	
		
    }
    
    /***************************/
    
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	toast("Service exit");
        mNM.cancel(NEW_DEVICE_NOTIFICATION);
        mUsbCommHandler.deRegister();
    }

    private void toast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    public class LocalBinder extends Binder {        
		public HNADService getService() {        
			
			return HNADService.this;        
		}    
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
    
	private void showNewDeviceNotification(Device device) {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		//TODO go to the device details for this new device
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + device.UID();        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_wifi_signal_0, text, System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		Intent intent = new Intent(this, HnadConfigurationTabActivity.class);
		intent.putExtra("device", device); // TODO fill actual device
		intent.putExtra("clearNotifications",true); // TODO fill actual device
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification. 
		notification.vibrate = new long[]{Notification.DEFAULT_VIBRATE};
		mNM.notify(NEW_DEVICE_NOTIFICATION, notification);    
	}

	//TODO make into enum
	public class Events{
		public static final String HNAD_CORE_EVENT_MSG = "cste.hnad.android.HNAD_CORE_EVENT";
		public static final String DEVICE_INFO_CHANGED = "DEVICE_INFO_CHANGED";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
		public static final String USB_STATE_CHANGED = "USB CONNECTED";
	}
	
	public static final int NEW_DEVICE_NOTIFICATION = 0;

	
}
//startService(new Intent(this,UsbService.class));