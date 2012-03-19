package cste.android.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.ECoCInfoActivity;
import cste.android.activities.LoginActivity;
import cste.android.db.DbHandler;
import cste.android.network.NetworkHandler;
import cste.hnad.CsdMessageHandler;
import cste.icd.components.ComModule;
import cste.icd.components.ECoC;
import cste.icd.icd_messages.EventLogICD;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.icd_messages.RestrictedStatus;
import cste.icd.types.ConveyanceID;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import cste.icd.types.EcmEventLogType;
import cste.icd.types.EcocCmdType;
import cste.icd.types.GpsLoc;
import cste.icd.types.IcdTimestamp;
import cste.icd.types.MsgType;
import cste.icd.types.NadEventLogType;
import cste.icd.types.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import cste.misc.HnadEventLog;
import cste.misc.IcdTxItem;
import cste.misc.XbeeAPI;
import cste.misc.XbeeFrame;

/***
 * HNAD backgroud service
 * @author Sergio Enriquez
 *
 */
public class HNADService extends Service implements KeyProvider{
	private static final String TAG = "HNAD Service";
	
	private NADABroadcaster 	mNadaBroadcaster;
	private CsdMessageHandler 	mCsdMessageHandler;
	private UsbCommManager 		mUsbCommHandler;
	private NetworkHandler 		mNetworkHandler;
	private NotificationManager mNM;
	private SharedPreferences 	mSettings;
	private Handler 			mNadaHandler = new Handler();
	private List<IcdMsg> 		mWaitingMsgList;
	private List<IcdMsg> 		mSentMsgList;
	private List<String> 		mWaypointList;
	private Timer 				mUsbReconnectTimer;
	private Timer 				mDcpHeartbeatTimer;
	private DbHandler 			db;
	
	private int mWaypointIndex 	= 0;
	private final IBinder mBinder = new LocalBinder();
	
	private String mDcpUsername;
	private ConveyanceID conveyanceID;//TODO use the custom class
	
	public void toggleDiscoveryMode(boolean state){
		mNadaBroadcaster.setDeviceDiscoveryMode(state);
	}

	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mNetworkHandler = new NetworkHandler(this);
		mDcpUsername = "NA";
		
		db = new DbHandler(this);
        db.open();
        db.resetTempDeviceVars(); //clears rssi,visible,pendingTx vars
        db.storeHnadLog(NadEventLogType.POWER_ON, mDcpUsername);

        mSentMsgList = new ArrayList<IcdMsg>(5);
        mWaitingMsgList = new ArrayList<IcdMsg>(5);
        mWaypointList = new ArrayList<String>();

		mCsdMessageHandler = new CsdMessageHandler(this);
		mUsbCommHandler = new UsbCommManager(this,mCsdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this,mNadaHandler,mUsbCommHandler);
		mUsbReconnectTimer = null;
		
		XbeeAPI.setRadioInterface(mUsbCommHandler);
		XbeeAPI.setHnadService(this);
		
		mSettings = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);
		loadSettings();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        
        //if ( mIsLoggedIn){
		//	Intent devListIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
		//	devListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    startActivity(devListIntent);
		//}else{
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
		//}
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

	
	/**********************/
	/**********************/
	
	public void onLoginResult(boolean result){
		if(result)
			db.storeHnadLog(NadEventLogType.LOGIN_SUCCESS, mDcpUsername);
		else
			db.storeHnadLog(NadEventLogType.LOGIN_FAILURE, mDcpUsername);
	}

	public void login(String username, String dcpPassword) {
		mDcpUsername = username;
		mNetworkHandler.loginToDCP(mDcpUsername, dcpPassword);
		
		//assume log in was successful for now, connect USB device
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
	

	// Stops transmitting or receiving 802.15.4 messages
	public void logout() {
		stopRadioComm();
	}

	public void uploadData() {
		//transmit all stored ECM log messages
		//transmit all stored HNAD log messages
		// TODO Auto-generated method stub	
	}
	
	public void setDeviceTCK(DeviceUID devUID, byte []newTCK){
		ComModule destDev = db.getDevice(devUID);

		if( destDev == null){
			Log.e(TAG, "Tried to set TCK on a device that does not exist");
			return;
		}
		destDev.setTCK(newTCK);
		db.storeDevice(destDev);
		
		Intent intent = new Intent(Events.DEVICE_INFO_CHANGED);
		intent.putExtra("deviceUID",devUID);
		sendBroadcast(intent);
	}
	
    public void setDeviceAssensionVal(DeviceUID devUID, int val){
    	ComModule destDev = db.getDevice(devUID);
		if( destDev == null){
			Log.e(TAG, "Tried to set assension on a device that does not exist");
			return;
		}
		destDev.txAscension = val;
		db.storeDevice(destDev);
		
		Intent intent = new Intent(Events.DEVICE_INFO_CHANGED);
		intent.putExtra("deviceUID",devUID);
		sendBroadcast(intent);
    }

	public void sendDevCmd(DeviceUID destUID, DeviceCommands cmd) {
		ComModule destDev = db.getDevice(destUID);
		if( destDev == null){
			Log.w(TAG, destUID.toString() + " not stored, cannot send command");
			return;
		}
		
		if(!this.mUsbCommHandler.isReady()){
			toast("Cannot transmit, USB not availible");
			//TODO notifyOfTransmissionResult(false,destUID); // if interface not available now
		}

		IcdMsg icdMsg = null;
		switch(cmd){
		case SEND_ACK:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.ACK, (byte)destDev.rxAscension);
			break;
		case SET_TIME:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.ST, IcdTimestamp.now());
			break;
		case SET_TRIPINFO:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.CWT,conveyanceID);
			break;
		case SET_WAYPOINTS_START:
			if(this.mWaypointList.size() > 0){
				mWaypointIndex = 0;
				GpsLoc gps = new GpsLoc(mWaypointList.get(mWaypointIndex++));
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.WLN, gps);
			}else{
				toast("No waypoints set");
				return;
			}
			break;
		case SET_WAYPOINTS_NEXT:
			if(mWaypointIndex < mWaypointList.size()){
				GpsLoc gps = new GpsLoc(mWaypointList.get(mWaypointIndex++));
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.WLA,gps);
			}
			else{
				toast("Invalid waypoint index");
				return;
			}
			break;
		case SET_ALARM_OFF:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAT);
			break;
		case SET_ALARM_ON:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAF);
			break;
		case SET_COMMISION_OFF:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.DAHH);
			break;
		case SET_COMMISION_ON:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAT);
			break;
		case GET_RESTRICTED_STATUS:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.NOP);
			break;
		case GET_UN_RESTRICTED_STATUS:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_UNRESTRICTED, UnrestrictedCmdType.REQUEST);
			break;
		case GET_EVENT_LOG:
			mWaypointIndex = 0;
			if( db.getDevLogRecordCount(destUID) == 0 )
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SL);
			else
				icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SLU);
			break;
		case CLEAR_EVENT_LOG:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.EL);
			break;
		default:
			Log.w(TAG, "Command not supported");
			return;
		}

		mWaitingMsgList.add(icdMsg);
		mNadaBroadcaster.updateWaitingList();
		destDev.txAscension++;
		db.storeDevice(destDev);
	}

	protected void notifyOfTransmissionResult(boolean success, DeviceUID destUID){
		Intent intent = new Intent(Events.TRANSMISSION_RESULT);
    	intent.putExtra("result", success);
    	intent.putExtra("deviceUID", destUID);
		sendBroadcast(intent);
	}

	public void onFrameReceived(XbeeFrame frm) {
    	if( frm.type == XbeeAPI.RX_64BIT){
    		IcdMsg msg = IcdMsg.fromBytes(frm.payload);
    		if( msg.msgStatus == IcdMsg.MsgStatus.OK){
    			String key = msg.header.devUID.toString();
    			ECoC deviceSrc = (ECoC)db.getDevice(msg.header.devUID);
    	    	if( deviceSrc == null ){
    	    		deviceSrc = new ECoC( msg.header.devUID , frm.address);
    	    		db.storeDevice(deviceSrc);

    	    		Intent intent = new Intent(Events.DEVLIST_CHANGED);
    	    		intent.putExtra(Events.DEVLIST_CHANGED,key);
    	    		sendBroadcast(intent);
    	    		showNewDeviceNotification(deviceSrc);
    	    	}
    	    	deviceSrc.inRange = true;
    	    	deviceSrc.rssi = (byte)frm.rssi;
    	    	deviceSrc.rxAscension = msg.header.msgAsc;
    	    	db.storeDevice(deviceSrc);
    			handleIcdMsg(msg,deviceSrc);
    		}else
    			Log.w(TAG,"Received ICD msg with an error: " + msg.msgStatus.name());
    	}
	}

	private void onMsgResponseReceived(IcdMsg msgRec){
		IcdMsg msgSent = null;
    	for(IcdMsg msg : mSentMsgList){
    		if( msg != null && msg.destUID.equals(msgRec.header.devUID)){
    			//DB hnadlog store
    			msgSent = msg;
    			db.storeHnadLog(NadEventLogType.ICD_MSG_RECEIVED, mDcpUsername, msgSent, msgRec);
    			
    			break;//Tx only 1 msg at a time
    		}
    	}
    	
    	if( msgSent != null)
    		mSentMsgList.remove(msgSent);
	}

	
	
    /***
     * When NULL msg is received, transmit one buffered msg for the receiver
     * @param uid
     */
    private void onNullMsgReceived(DeviceUID uid){
    	IcdMsg msgSent = null;
    	for(IcdMsg msg : mWaitingMsgList){
    		if( msg != null && msg.destUID.equals(uid)){
    			ComModule cm = this.getDeviceRecord(msg.destUID);
    			XbeeAPI.transmitPkt( cm.address ,msg.getBytes());
    			mSentMsgList.add(msg);
    			msgSent = msg;
    			break;//Tx only 1 msg at a time
    		}
    	}
    	
    	if( msgSent != null){
    		mWaitingMsgList.remove(msgSent);
    		mNadaBroadcaster.updateWaitingList();
    	}
    }

    /***
     * 
     * @param msg
     */
    private void handleIcdMsg(IcdMsg msg, ComModule deviceSrc){
    	//TODO log this HNAD event
    	short ackNo;
    	switch(msg.header.msgType){
    	case RESTRICTED_STATUS_MSG:
    		RestrictedStatus status = (RestrictedStatus)msg.payload;
    		deviceSrc.setRestrictedStatus((RestrictedStatus)msg.payload);
    		db.storeDevice(deviceSrc);
    		ackNo = (short) (status.ackNo & 0xFF);
    		onMsgResponseReceived(msg);
    		
    		if( mWaypointIndex > 0){ //setting waypoints
    			if( mWaypointIndex < mWaypointList.size()){
    				toast("Sending waypoint " + String.valueOf(mWaypointIndex));
    				sendDevCmd(deviceSrc.devUID, DeviceCommands.SET_WAYPOINTS_NEXT );
    			}else{
    				toast("Done sending waypoints");
    				mWaypointIndex = 0;
    				notifyOfTransmissionResult(true, deviceSrc.devUID);
    			}
    		}else{
    			//onRadioTransmitResult(true,deviceSrc.devUID,ackNo);
        		notifyOfTransmissionResult(true, deviceSrc.devUID);
    		}
    		break;
    	case DEVICE_EVENT_LOG:
    		EventLogICD logRecord = (EventLogICD)msg.payload;
    		ackNo = (short) (logRecord.ackNo & 0xFF);
    		db.storeDevLog(deviceSrc.devUID, logRecord);
    		onMsgResponseReceived(msg);
    		
    		//onRadioTransmitResult(true,deviceSrc.devUID,ackNo);
    		sendDevCmd(deviceSrc.devUID,DeviceCommands.SEND_ACK);
    		Log.i(TAG,"Received record, ACK " + String.valueOf(ackNo));
    		toast("Rec Log " + String.valueOf(ackNo));
    		if( logRecord.eventType == EcmEventLogType.END_OF_RECORDS){
    			Intent intent = new Intent(Events.ECM_EVENTLOG_CHANGE);
    			intent.putExtra("deviceUID",deviceSrc.devUID);
    			sendBroadcast(intent);
    		}
    		break;
    	case NULL_MSG:
    		onNullMsgReceived(deviceSrc.devUID);
    		break;
    	case UNRESTRICTED_STATUS_MSG:
    		ackNo = (short) ( deviceSrc.rxAscension & 0xFF);
    		//toast("Received unrestricted status");
    		//notifyOfTransmissionResult(true, deviceSrc.devUID);
    		break;
    	default:
    		ackNo = (short) ( deviceSrc.rxAscension & 0xFF);
    	}

    	Intent testIntent = new Intent(Events.DEVICE_INFO_CHANGED);
		testIntent.putExtra("deviceUID",deviceSrc.devUID);
		sendBroadcast(testIntent);
    }

	public ComModule getDeviceRecord(DeviceUID devUID){
		return db.getDevice(devUID);
	}

	public void deleteDeviceRecord(DeviceUID devUID){
		db.deleteDeviceRecord(devUID);
		Intent intent = new Intent(Events.DEVLIST_CHANGED);
		intent.putExtra("deviceUID",devUID);
		sendBroadcast(intent);
	}
	
	public void saveGeneralSettings(){
		//TODO for now reload all
		loadSettings();
	}

    /***************************/
    /***************************/

	public List<String> getWaypointList(){
		return mWaypointList;
	}
	
	public String getConveyanceIDStr(){
		return conveyanceID.toString();
	}

	public List<IcdMsg> getTxList(){
		return mWaitingMsgList;
	}

	public Hashtable<DeviceUID,ComModule> getDeviceList(){
		return db.getStoredDevices();
	}

	public void deleteDeviceLogs(DeviceUID devUID){
		db.deleteDevLogRecords(devUID);
	}
	
	public ArrayList<EventLogICD> getEcmEventLog(DeviceUID devUID){
		return db.getDevLogRecords(devUID);
	}
	
	public ArrayList<HnadEventLog> getHnadEventLog(){
		return db.getHnadLogRecords();
	}
	
	public SharedPreferences getSettingsFile(){
		return mSettings;
	}

	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		ComModule cm = db.getDevice(destinationUID);
		if( cm == null ){
			Log.w(TAG,"No key availible for " + destinationUID.toString());
			return null;
		}
		if( cm.keyValid )
			return cm.getTCK();
		else
			return null;
	}

	public boolean getUsbState(){
		return this.mUsbCommHandler.isReady();
	}

    public void onUsbStateChanged(boolean connected){
    	Intent intent = new Intent(Events.USB_STATE_CHANGED);
    	if(connected){
    		toast("USB is connected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		mNadaHandler.post(mNadaBroadcaster);
    		intent.putExtra("usbState",true);
    	}
    	else{
    		toast("USB was disconnected");
    		mNadaHandler.removeCallbacks(mNadaBroadcaster);
    		intent.putExtra("usbState",false);
    	}
    	sendBroadcast(intent);
    }

    /***
	 * Reload settings from the preferences file and confiure the ICD msg class
	 */
	protected void loadSettings(){
		boolean useEncryption = mSettings.getBoolean(SettingsKey.USE_ENC, false);
		String thisUIDStr = mSettings.getString(SettingsKey.THIS_UID, "0D0E0A0D0B0E0E0F");
		String dcpUIDStr = mSettings.getString(SettingsKey.DCP_UID,   "0000000000000000");
		int burstIndex = mSettings.getInt(SettingsKey.NADA_BURST, 5);
		
		DeviceUID thisUID = new DeviceUID(thisUIDStr);
		DeviceUID dcpUID = new DeviceUID(thisUIDStr);
		
		IcdMsg.configure(useEncryption,DeviceType.FNAD_I, thisUID, this);	
		mNadaBroadcaster.config(burstIndex, DeviceType.INVALID, new DeviceUID(dcpUIDStr), DeviceType.DCP, dcpUID);
		
		String temp =  mSettings.getString(SettingsKey.CONVEYANCE_ID, "ConveyanceID");;
		conveyanceID = new ConveyanceID(temp);
		String waypointStr = mSettings.getString(SettingsKey.WAYPOINT_LIST, "A4807.038N001131.000E,A4111.033N002222.001E");
		if( waypointStr != ""){
			String[] waypointArr = waypointStr.split(",");
			mWaypointList = Arrays.asList(waypointArr);
		}
	}
	
    protected void stopRadioComm(){
		mNadaHandler.removeCallbacksAndMessages(mNadaBroadcaster);
		mUsbCommHandler.closeDevice();
		if( mUsbReconnectTimer != null)
			mUsbReconnectTimer.cancel();
	}
    
    @Override
    public void onDestroy() {
    	toast("Service exit");
    	db.storeHnadLog(NadEventLogType.POWER_OFF, mDcpUsername);
    	db.close();
        mNM.cancel(Events.NEW_DEVICE_DETECTED); // Cancel the persistent notification.

        stopRadioComm();
    }

    private void toast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
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
		return mBinder;
	}
    
	private void showNewDeviceNotification(ComModule device) {          
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + device.devUID;        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_signal_4, text, java.lang.System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		Intent intent = new Intent(this, ECoCInfoActivity.class);
		intent.putExtra("deviceUID", device.devUID);
		intent.putExtra("clearNotifications",true);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification. 
		notification.vibrate = new long[]{Notification.DEFAULT_VIBRATE};
		mNM.notify(Events.NEW_DEVICE_DETECTED, notification);    
	}
	
	public void saveWaypointSettings(String conveyanceStr, ArrayList<String> waypoints){
		conveyanceID = new ConveyanceID(conveyanceStr);
		mWaypointList = waypoints;
		
    	SharedPreferences settings = getSettingsFile();
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putString(SettingsKey.CONVEYANCE_ID, conveyanceStr);
    	StringBuilder sb = new StringBuilder("");
    	for(String s:waypoints){
    		sb.append(s);
    		sb.append(",");
    	}
    	editor.putString(SettingsKey.WAYPOINT_LIST, sb.toString());
    	editor.commit();
	}
	
	public class SettingsKey{
		public static final String DCP_USERNAME = "USERNAME";
		public static final String DCP_PASSWORD = "PASSWORD";
		public static final String REMEMBER_PASS = "REMEMBER_PASS";
		
		public static final String THIS_UID 	= "THIS_UID";
		public static final String DCP_UID 		= "DCP_UID";
		public static final String DCP_ADDR 	= "DCP_ADDR";
		public static final String DCP_PORT 	= "DCP_PORT";
		public static final String FTP_ADDR 	= "FTP_ADDR";
		public static final String USE_ENC 		= "USE_ENC";
		public static final String NADA_BURST 	= "NADA_BURST";
		
		public static final String CONVEYANCE_ID = "CONVEYANCE_ID";
		public static final String WAYPOINT_LIST = "WAYPOINT_LIST";
	}

	public class Events{
		public static final String DEVICE_INFO_CHANGED 	= "DEVICE_INFO_CHANGED";
		public static final String DEVLIST_CHANGED 		= "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT 		= "LOGIN_RESULT";
		public static final String UPLOAD_DATA 			= "UPLOAD_DATA";
		public static final String USB_STATE_CHANGED 	= "USB CONNECTED";
		public static final String TRANSMISSION_RESULT 	= "USB TRANSMISSION_RESULT";
		public static final String ECM_EVENTLOG_CHANGE 	= "USB TRANSMISSION_RESULT";
		public static final String HNAD_EVENTLOG_CHANGE = "HNAD_EVENT_LOG_CHANGD";
		public static final int    NEW_DEVICE_DETECTED	= 0;
	}
	
	public enum DeviceCommands{
		SEND_ACK,
		SET_TIME,
		SET_TRIPINFO,
		SET_WAYPOINTS_START,
		SET_WAYPOINTS_NEXT,
		SET_ALARM_OFF,
		SET_ALARM_ON,
		SET_COMMISION_OFF,
		SET_COMMISION_ON,
		GET_RESTRICTED_STATUS,
		GET_UN_RESTRICTED_STATUS,
		GET_EVENT_LOG,
		CLEAR_EVENT_LOG
	}
}
//startService(new Intent(this,UsbService.class));