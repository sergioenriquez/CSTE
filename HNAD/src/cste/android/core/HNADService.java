package cste.android.core;

import java.io.Serializable;
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
import cste.icd.EventLogType;
import cste.icd.MsgType;
import cste.icd.EcocCmdType;
import cste.icd.IcdTimestamp;
import cste.icd.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import cste.messages.EventLogICD;
import cste.messages.IcdMsg;
import cste.messages.RestrictedCmdECM;
import cste.messages.RestrictedStatus;
import cste.misc.IcdTxItem;
import cste.misc.XbeeAPI;
import cste.misc.XbeeFrame;
import static cste.icd.Utility.strToHex;

//import org.apache.commons.collections.map.LinkedMap;
//import org.apache.commons.collections.map.MultiKeyMap;
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

	private boolean mIsLoggedIn = false;
	private NADABroadcaster mNadaBroadcaster;
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommManager mUsbCommHandler;
	private NetworkHandler mNetworkHandler;
	private NotificationManager mNM;
	private Handler mNadaHandler = new Handler();
	private DbHandler db;
	//private Hashtable<DeviceUID,ComModule> mDevTable;
	private final IBinder mBinder = new LocalBinder();
	private Timer mUsbReconnectTimer;
	private Hashtable<DeviceUID,IcdTxItem> mIcdTxMap;// =  MultiKeyMap.decorate(new LinkedMap(10));
	private SharedPreferences settings;// = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);

	@Override
	public void test(){
		//db.storeDevLog(new DeviceUID("0013A20040715FD8"), IcdTimestamp.now() , EventLogType.CHANGE_IN_ALARM_STATUS_CSD, new byte[61]);
	}
	
	@Override
	public Hashtable<DeviceUID,ComModule> getDeviceList(){
		return db.getStoredDevices();
	}
	
	@Override
	public void deleteDeviceLogs(DeviceUID devUID){
		db.deleteDevLogRecords(devUID);
	}
	
	@Override
	public ArrayList<EventLogICD> getDeviceEventLog(DeviceUID devUID){
		return db.getDevLogRecords(devUID);
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NetworkHandler.setServiceHost(this);
		
		db = new DbHandler(this);
        db.open();
        db.resetTempDeviceVars(); //clears rssi,visible,pendingTx vars
               
        EcocDevice e5 = new EcocDevice(new DeviceUID("0022334455667788"));
        e5.armedStatus = -1;
        db.storeDevice(e5);
        
        EcocDevice e4 = new EcocDevice(new DeviceUID("1122334455667788"));
        e4.armedStatus = 1;
        db.storeDevice(e4);
        
        EcocDevice e2 = new EcocDevice(new DeviceUID("2222334455667788"));
        e2.tck = new byte[16];
        db.storeDevice(e2);
        
        EcocDevice e1 = new EcocDevice(new DeviceUID("3322334455667788"));
        e1.errors = 3;
        db.storeDevice(e1);

        mIcdTxMap = new Hashtable<DeviceUID,IcdTxItem>(5);
		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommManager(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		mUsbReconnectTimer = null;
		XbeeAPI.setRadioInterface(mUsbCommHandler);
		XbeeAPI.setHnadService(this);
		
		settings = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);
		loadSettings();

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
	
	
	/**********************/
	/**********************/
	/**********************/

	@Override
	public void login(String username, String password) {
		// TODO Auto-generated method stub
		
		//assume log in was successful, connect USB device
		Intent intent = new Intent(Events.LOGIN_RESULT);
		intent.putExtra("result",true);
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
	}

	@Override
	public void uploadData() {
		// TODO Auto-generated method stub	

	}
	
    @Override 
    public void setDeviceAssensionVal(DeviceUID devUID, int val){
    	ComModule destDev = db.getDevice(devUID);
		if( destDev == null){
			Log.e(TAG, "Tried to set assension on a device that does not exist");
			return;
		}
		destDev.txAscension = val;
		db.storeDevice(destDev);
    }

	@Override
	public void sendDevCmd(DeviceUID destUID, DeviceCommands cmd) {
		int hash = destUID.hashCode();
		ComModule destDev = db.getDevice(destUID);
		if( destDev == null){
			Log.w(TAG, destUID.toString() + " not stored, cannot send command");
			return;
		}

		IcdMsg icdMsg = null;
		
		switch(cmd){
		case SEND_ACK:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.ACK, (byte)destDev.rxAscension);
			break;
		case SET_TIME:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.ST, IcdTimestamp.now());
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
			if( db.getDevLogRecordCount(destUID) == 0 )
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SL);
			else
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SLU);
			break;
		case CLEAR_EVENT_LOG:
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.EL);
			break;
		default:
			Log.w(TAG, "Command not supported");
			return;
		}

		byte[] destAddrs = destDev.UID().getBytes();
		if( !XbeeAPI.transmitPkt(destAddrs,icdMsg.getBytes()) )
			notifyOfTransmissionResult(false,destUID); // if interface not available now
		else{
			short ackNo = (short) (destDev.txAscension & 0xFF);
			Log.i(TAG,"Sent " + cmd.toString() + " , ACK: " + String.valueOf(ackNo));
			addItemToTxMap(destDev.UID(), icdMsg);
		}

		destDev.pendingTxMsgCnt++;
		destDev.txAscension++;
		db.storeDevice(destDev);
	}
	
	protected void addItemToTxMap(DeviceUID destUID, IcdMsg icdMsg){
		IcdTxItem txItem = mIcdTxMap.get(destUID);
		if( txItem != null){
			// Cancel any existing retransmission items for this destination
			txItem.clearTimer();
			txItem.retryAttempts = 0;
			txItem.msgSent = icdMsg;
		}
		else{
			txItem = new IcdTxItem(this,destUID,icdMsg);
			mIcdTxMap.put(destUID, txItem);
		}
	}
	
	@Override
    public void onRadioTransmitResult(boolean result, DeviceUID destUID, short ackNo){
		ComModule destDev = db.getDevice(destUID);
    	if( destDev == null){
    		Log.e(TAG,"Missing device record for " + destDev.toString());
    		return;
    	}

    	IcdTxItem txItem = mIcdTxMap.get(destUID);
    	if( txItem == null){
    		//Log.e(TAG, "Missing tx item for " + destUID.toString());
    		//notifyOfTransmissionResult(result,destUID);
    		return;
    	}
    	
    	short ackNoSent = (short)(txItem.msgSent.headerData.msgAsc & 0xFF);
    	if( ackNoSent > ackNo ){
    		Log.e(TAG, "Received an invalid AckNo for " + destUID.toString() + ":" + String.valueOf(ackNo));
    		return;
    	}
    	
    	txItem.clearTimer();
    	IcdMsg msgSent = txItem.msgSent;   	

    	if( result == false){
    		Log.i(TAG,"Timeout for ACK: " + String.valueOf(ackNo));
	    	if( txItem.retryAttempts > 5){
	    		destDev.pendingTxMsgCnt--;
	    		toast("No reply received from " + destUID.toString() + "for AckNo: " + String.valueOf(ackNo));
	    		notifyOfTransmissionResult(false,destUID);
	    		mIcdTxMap.remove(destUID);
	    	}else{
	    		byte[] destAddrs = destUID.getBytes();
	    		msgSent.headerData.msgAsc = destDev.txAscension++;
	    		db.storeDevice(destDev);
	    		XbeeAPI.transmitPkt(destAddrs, msgSent.getBytes()); 
	    		txItem.msgSent = msgSent;
	    		txItem.retryAttempts++;
	    		txItem.restartTimer();
	    	}
    	}else{
    		mIcdTxMap.remove(destUID);
    	}
    }
	
	protected void notifyOfTransmissionResult(boolean success, DeviceUID destUID){
		Intent intent = new Intent(Events.TRANSMISSION_RESULT);
    	intent.putExtra("result", success);
    	intent.putExtra("deviceUID", destUID);
		sendBroadcast(intent);
	}

    @Override
	public void onFrameReceived(XbeeFrame frm) {
    	if( frm.type == XbeeAPI.RX_64BIT){
    		IcdMsg msg = IcdMsg.fromBytes(frm.payload);
    		if( msg.msgStatus == IcdMsg.MsgStatus.OK){
    			String key = msg.headerData.devUID.toString();
    			EcocDevice deviceSrc = (EcocDevice)db.getDevice(msg.headerData.devUID);
    	    	if( deviceSrc == null ){
    	    		deviceSrc = new EcocDevice( msg.headerData.devUID);
    	    		db.storeDevice(deviceSrc);

    	    		Intent intent = new Intent(Events.DEVLIST_CHANGED);
    	    		intent.putExtra(Events.DEVLIST_CHANGED,key);
    	    		sendBroadcast(intent);
    	    		showNewDeviceNotification(deviceSrc);
    	    	}
    	    	deviceSrc.inRange = true;
    	    	deviceSrc.rssi = (byte)frm.rssi;
    	    	deviceSrc.rxAscension = msg.headerData.msgAsc;
    	    	db.storeDevice(deviceSrc);
    			handleIcdMsg(msg,deviceSrc);
    		}else
    			Log.w(TAG,"Received ICD msg with an error: " + msg.msgStatus.name());
    	}
	}

    /***
     * 
     * @param msg
     */
    private void handleIcdMsg(IcdMsg msg, ComModule deviceSrc){

    	//TODO log this HNAD event
    	short ackNo;
    	switch(msg.headerData.msgType){
    	case RESTRICTED_STATUS_MSG:
    		RestrictedStatus status = (RestrictedStatus)msg.payload;
    		deviceSrc.setRestrictedStatus((RestrictedStatus)msg.payload);
    		ackNo = (short) (status.ackNo & 0xFF);
    		
    		onRadioTransmitResult(true,deviceSrc.UID(),ackNo);
    		notifyOfTransmissionResult(true, deviceSrc.UID());
    		break;
    	case DEVICE_EVENT_LOG:
    		EventLogICD logRecord = (EventLogICD)msg.payload;
    		ackNo = (short) (logRecord.ackNo & 0xFF);
    		db.storeDevLog(deviceSrc.UID(), logRecord);
    		
    		onRadioTransmitResult(true,deviceSrc.UID(),ackNo);
    		sendDevCmd(deviceSrc.UID(),DeviceCommands.SEND_ACK);
    		Log.i(TAG,"Received record, ACK " + String.valueOf(ackNo));
    		
    		if( logRecord.eventType == EventLogType.END_OF_RECORDS){
    			Intent intent = new Intent(Events.DEV_EVENT_LOG_CHANGD);
    			intent.putExtra("deviceUID",deviceSrc.UID());
    			sendBroadcast(intent);
    		}
    		break;
    	case DEV_CMD_RESTRICTED:
    		RestrictedCmdECM cmd = (RestrictedCmdECM)msg.payload;
    		byte temp = (Byte)cmd.params[0];
    		ackNo = (short) (temp& 0xFF);

    		onRadioTransmitResult(true,deviceSrc.UID(),ackNo);
    		break;
    	case UNRESTRICTED_STATUS_MSG:
    		ackNo = (short) ( deviceSrc.rxAscension & 0xFF);
    		//TODO use this data
    		//no retransmission entry was created for this reply, do nothing
    		//onRadioTransmitResult(true,deviceSrc.UID(), (byte)0);
    		break;
    	default:
    		ackNo = (short) ( deviceSrc.rxAscension & 0xFF);
    	}

    	Intent testIntent = new Intent(Events.DEVICE_INFO_CHANGED);
		testIntent.putExtra("deviceUID",deviceSrc.UID());
		sendBroadcast(testIntent);
    }

    @Override
	public ComModule getDeviceRecord(DeviceUID devUID){
		return db.getDevice(devUID);
	}
	
	@Override
	public void deleteDeviceRecord(DeviceUID devUID){
		db.deleteDeviceRecord(devUID);
		
		Intent intent = new Intent(Events.DEVLIST_CHANGED);
		intent.putExtra("deviceUID",devUID);
		sendBroadcast(intent);
	}

	@Override
	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		byte[] key = new byte[16];
		//TODO use database to retrieve key from device record
		return strToHex("1234567890ABCDEF1234567890ABCDEF"); 
	}

    /***************************/
    /***************************/
    /***************************/
    /***************************/

    @Override
    public void onUsbStateChanged(boolean connected){
    	Intent intent = new Intent(Events.USB_STATE_CHANGED);
    	if(connected){
    		toast("USB is connected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		mNadaHandler.post(mNadaBroadcaster);
    		
    		intent.putExtra("usbState",true);
    		sendBroadcast(intent);
    	}
    	else{
    		toast("USB was disconnected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		
    		intent.putExtra("usbState",false);
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
    
    @Override
    public Context getContext(){
    	return getApplicationContext();
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
		Notification notification = new Notification(R.drawable.stat_sys_signal_4, text, System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
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
		//public static final String HNAD_CORE_EVENT_MSG = "cste.hnad.android.HNAD_CORE_EVENT";
		public static final String DEVICE_INFO_CHANGED = "DEVICE_INFO_CHANGED";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
		public static final String USB_STATE_CHANGED = "USB CONNECTED";
		public static final String TRANSMISSION_RESULT = "USB TRANSMISSION_RESULT";
		public static final String DEV_EVENT_LOG_CHANGD = "USB TRANSMISSION_RESULT";
	}
	
	public enum DeviceCommands{
		SEND_ACK,
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
	
	public static final int NEW_DEVICE_NOTIFICATION = 0;

	
}
//startService(new Intent(this,UsbService.class));