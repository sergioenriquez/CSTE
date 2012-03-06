package cste.android.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.ECoCInfoActivity;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.LoginActivity;
import cste.android.db.DbHandler;
import cste.components.ComModule;
import cste.hnad.CsdMessageHandler;
import cste.hnad.EcocDevice;
import cste.hnad.HNADServiceInterface;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;
import cste.icd.EcocCmdType;
import cste.icd.TimeStamp;
import cste.icd.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import cste.messages.IcdMsg;
import cste.messages.RestrictedStatus;
import cste.misc.XbeeAPI;
import cste.misc.XbeeFrame;
import static cste.icd.Utility.strToHex;

/***
 * 
 * @author Sergio Enriquez
 *
 */
public class HNADService extends Service implements HNADServiceInterface, KeyProvider{
	private static final String TAG = "HNAD Core Service";
	//Test ECOC device does not recognize secure HNAD type? Will use FNAD for now...

	public DeviceType dcpDevType 	= DeviceType.INVALID;
	public DeviceUID dcpUID 		= new DeviceUID("0000000000000000");
	public DeviceType lvl2DevType 	= DeviceType.HNAD_S;
	public DeviceUID lvl2UID 		= new DeviceUID("0013A20040715FD8");
	public final byte icdRev 		= 0x02;//0x02
	
	List<DeviceUID> mMsgWaitingList;

	private boolean mIsLoggedIn = false;
	private NADABroadcaster mNadaBroadcaster;
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommManager mUsbCommHandler;
	private NetworkHandler mNetworkHandler;
	private NotificationManager mNM;
	private Handler mNadaHandler = new Handler();
	private DbHandler db;
	private Hashtable<String,ComModule> devTable;
	private final IBinder mBinder = new LocalBinder();
	private Timer mUsbReconnectTimer;

	private ProgressDialog pd;
	
	private void showProgressDialog(){
		pd = ProgressDialog.show(this, "Working..", "Refreshing Device Information", true, true);
	}

	/*****************************/

	private SharedPreferences settings;// = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);

	public Hashtable<String,ComModule> getDeviceList(){
		return devTable;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NetworkHandler.setServiceHost(this);
		db = new DbHandler(this);
        db.open();
        devTable = db.getStoredDevices();
		
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommManager(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		mUsbReconnectTimer = null;
		XbeeAPI.setRadioInterface(mUsbCommHandler);
		
		settings = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);
		loadSettings();//TODO X
		devTable.put("0013A20040715FD8", new EcocDevice(new DeviceUID("0013A20040715FD8")));
		//mDeviceMap.put("0013A20040760BB4", new Device(new DeviceUID("0013A20040760BB4"),DeviceType.ACSD));
		//devTable.put("0013A200406BE0C3", new EcocDevice(new DeviceUID("0013A200406BE0C3")));

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
		
		mUsbReconnectTimer = new Timer("USB reconnect timer",true);
		mUsbReconnectTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if ( mUsbCommHandler.openDevice() )
					this.cancel();
			}}, 100, 1000);// delay start 100ms, retry every second
	}
	
	@Override
	/***
	 * Stops transmitting or receiving 802.15.4 messages
	 */
	public void logout() {
		stopRadioComm();
		//save data to database
		Enumeration<ComModule> devices = devTable.elements();
		while(devices.hasMoreElements())
			db.storeDevice(devices.nextElement());
	}

	@Override
	public void uploadData() {
		// TODO Auto-generated method stub	

	}

	@Override
	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		byte[] key = new byte[16];
		//TODO use database to retrieve key from device record
		return strToHex("1234567890ABCDEF1234567890ABCDEF"); 
	}

	public enum DeviceCommands{
		SET_TIME,
		SET_TRIPINFO,
		SET_WAYPOINTS,
		SET_ALARM_OFF,
		SET_ALARM_ON,
		SET_COMMISION_OFF,
		SET_COMMISION_ON,
		GET_RESTRICTED_STATUS,
		GET_EVENT_LOG,
		CLEAR_EVENT_LOG
	}
	
	@Override
	public void sendDevCmd(DeviceUID destUID, DeviceCommands cmd) {
		ComModule destDev = this.devTable.get(destUID.toString());
		if( destDev == null){
			Log.w(TAG, destUID.toString() + " not stored, cannot send command");
			return;
		}

		byte []icdMsg = null;
		
		switch(cmd){
		case SET_TIME:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.ST, TimeStamp.now());
			break;
		case SET_TRIPINFO:
			//TODO Set conveyane id
			//icdMsg = IcdMsg.buildIcdMsg(dev, MsgType.DEV_CMD_RESTRICTED, CWT);
			break;
		case SET_WAYPOINTS:
			//TODO send waypoint list
			//icdMsg = IcdMsg.buildIcdMsg(dev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAF);
			break;
		case SET_ALARM_OFF:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAT);
			break;
		case SET_ALARM_ON:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAF);
			break;
		case SET_COMMISION_OFF:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.DAHH);
			break;
		case SET_COMMISION_ON:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAT);
			break;
		case GET_RESTRICTED_STATUS:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.NOP);
			break;
		case GET_EVENT_LOG:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SL);
			break;
		case CLEAR_EVENT_LOG:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.EL);
		default:
			Log.w(TAG, "Command not supported");
			return;
		}

		byte[] destAddrs = destDev.UID().getBytes();
		XbeeAPI.transmitPkt(destAddrs,icdMsg);
		//byte []zigbeeFrame = XbeeAPI.buildPkt(destDev.UID().getBytes(),(byte)(frameACK++),icdMsg);
		//this.mUsbCommHandler.transmit(zigbeeFrame);
	}


    @Override
	public void onFrameReceived(XbeeFrame frm) {
    	if( frm.type == XbeeAPI.RX_64BIT)
    	{
    		IcdMsg msg = IcdMsg.fromBytes(frm.payload);
    		if( msg.getStatus() == IcdMsg.MsgStatus.OK)
    		{
    			String key = msg.header().getDevUID().toString();
    			EcocDevice deviceSrc = (EcocDevice)devTable.get(key);

    	    	if( deviceSrc == null )
    	    	{
    	    		deviceSrc = new EcocDevice( msg.header().getDevUID());
    	    		devTable.put(key, deviceSrc);

    	    		Intent intent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.DEVLIST_CHANGED,key);
    	    		sendBroadcast(intent);
    	    		showNewDeviceNotification(deviceSrc);
    	    	}
    	    	deviceSrc.rssi = (byte)frm.rssi;
    			handleIcdMsg(msg,deviceSrc);
    		}
    		else
    			Log.w(TAG,"Received ICD msg with an error");
    	}
	}
    
    public void onRadioTransmitResult(boolean success){

    }
    
    /***
     * 
     * @param msg
     */
    private void handleIcdMsg(IcdMsg msg, ComModule deviceSrc)
    {
    	//TODO log this event
    	switch(msg.header().getMsgType()){
    	case RESTRICTED_STATUS_MSG:
    		deviceSrc.setRestrictedStatus((RestrictedStatus)msg.payload());
    		break;
    		default:
    	}
    	deviceSrc.setRxAsc(msg.header().getMsgAsc());
    	Intent testIntent = new Intent(Events.HNAD_CORE_EVENT_MSG).putExtra(Events.DEVICE_INFO_CHANGED,false);
		testIntent.putExtra("device",(Parcelable)deviceSrc);
		sendBroadcast(testIntent);
    }

    /***************************/

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
    
    public SharedPreferences getSettingsFile(){
		return settings;
	}
    
    /***
	 * Reload settings from the preferences file and confiure the ICD msg class
	 */
	protected void loadSettings(){
		boolean useEncryption = settings.getBoolean(SettingsKey.USE_ENC, false);
		String thisUIDStr = settings.getString(SettingsKey.THIS_UID, "0013A20040715FD8");
		String dcpUIDStr = settings.getString(SettingsKey.DCP_UID, "0013A20040715FD8");
		int burstIndex = settings.getInt(SettingsKey.NADA_BURST, 0);
		
		DeviceUID thisUID = new DeviceUID(thisUIDStr);
		DeviceUID dcpUID = new DeviceUID(dcpUIDStr);
		
		IcdMsg.configure(useEncryption,DeviceType.FNAD_I, thisUID, this);	
		mNadaBroadcaster.config(burstIndex, DeviceType.HNAD_S, thisUID, DeviceType.DCP, dcpUID);
	}
	
    protected void stopRadioComm(){
		mNadaHandler.removeCallbacksAndMessages(mNadaBroadcaster);
		mUsbCommHandler.closeDevice();
		if( mUsbReconnectTimer != null)
			mUsbReconnectTimer.cancel();
		}
    
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	toast("Service exit");
    	db.close();
        mNM.cancel(NEW_DEVICE_NOTIFICATION);

        stopRadioComm();
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
    
	private void showNewDeviceNotification(ComModule device) {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		//TODO go to the device details for this new device
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + device.UID();        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_wifi_signal_0, text, System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		Intent intent = new Intent(this, ECoCInfoActivity.class);
		intent.putExtra("device", (Parcelable)device); // TODO fill actual device
		intent.putExtra("clearNotifications",true); // TODO fill actual device
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification. 
		notification.vibrate = new long[]{Notification.DEFAULT_VIBRATE};
		mNM.notify(NEW_DEVICE_NOTIFICATION, notification);    
	}
	
	public class SettingsKey{
		public static final String USERNAME = "USERNAME";
		public static final String PASSWORD = "PASSWORD";
		public static final String REMEMBER_PASS = "REMEMBER_PASS";
		
		public static final String THIS_UID = "THIS_UID";
		public static final String DCP_UID = "DCP_UID";
		public static final String DCP_ADDR = "DCP_ADDR";
		public static final String FTP_ADDR = "FTP_ADDR";
		public static final String USE_ENC = "USE_ENC";
		public static final String NADA_BURST = "NADA_BURST";
	}

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