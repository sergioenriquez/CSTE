package cste.android.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
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
import android.os.Parcelable;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.ECoCInfoActivity;
import cste.android.activities.LoginActivity;
import cste.android.db.DbHandler;
import cste.components.ComModule;
import cste.hnad.CsdMessageHandler;
import cste.hnad.EcocDevice;
import cste.icd.ConveyanceID;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.EcocCmdType;
import cste.icd.EventLogType;
import cste.icd.GpsLoc;
import cste.icd.IcdTimestamp;
import cste.icd.MsgType;
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

/***
 * HNAD backgroud service
 * @author Sergio Enriquez
 *
 */
public class HNADService extends Service implements KeyProvider{
	private static final String TAG = "HNAD Core Service";
	public final byte icdRev 		= 0x02;//0x02
	private boolean mIsLoggedIn = false;
	private NADABroadcaster mNadaBroadcaster;
	private CsdMessageHandler mCsdMessageHandler;
	private UsbCommManager mUsbCommHandler;
	//private NetworkHandler mNetworkHandler;
	private NotificationManager mNM;
	private Handler mNadaHandler = new Handler();
	private DbHandler db;
	private ArrayList<IcdMsg> mTxList;
	private ArrayList<DeviceUID> mWaitingList;
	//private Hashtable<DeviceUID,ComModule> mDevTable;
	private final IBinder mBinder = new LocalBinder();
	private Timer mUsbReconnectTimer;
	private Hashtable<DeviceUID,IcdTxItem> mIcdTxMap;// =  MultiKeyMap.decorate(new LinkedMap(10));
	private SharedPreferences settings;// = getSharedPreferences("PreferencesFile", Context.MODE_PRIVATE);
	private List<String> mWaypointList;
	private int mWaypointIndex = 0;
	
	private ConveyanceID conveyanceID;//TODO use the custom class

	public void test(){
		
	}
	
	public void saveGeneralSettings(){
		//TODO for now reload all
		loadSettings();
	}
	
	
	public List<String> getWaypointList(){
		return mWaypointList;
	}
	
	public String getConveyanceIDStr(){
		return conveyanceID.toString();
	}
	
	private void updateWaitingList(){
		mWaitingList.clear();
		
		for(IcdMsg msg: mTxList){
			if(msg != null && !mWaitingList.contains(msg.destUID))
				mWaitingList.add(msg.destUID);
		}
	}
	
	public ArrayList<DeviceUID> getWaitingList(){
		return mWaitingList;
	}
	

	public Hashtable<DeviceUID,ComModule> getDeviceList(){
		return db.getStoredDevices();
	}

	public void deleteDeviceLogs(DeviceUID devUID){
		db.deleteDevLogRecords(devUID);
	}
	

	public ArrayList<EventLogICD> getDeviceEventLog(DeviceUID devUID){
		return db.getDevLogRecords(devUID);
	}

	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NetworkHandler.setServiceHost(this);
		
		db = new DbHandler(this);
        db.open();
        db.resetTempDeviceVars(); //clears rssi,visible,pendingTx vars
               
//        EcocDevice e5 = new EcocDevice(new DeviceUID("0000000000000000"));
//        db.storeDevice(e5);
//        
//        EcocDevice e4 = new EcocDevice(new DeviceUID("1111111111111111"));
//        db.storeDevice(e4);
//        
//        EcocDevice e2 = new EcocDevice(new DeviceUID("2222222222222222"));
//        db.storeDevice(e2);
//        
//        EcocDevice e1 = new EcocDevice(new DeviceUID("0013A20040715FD8"));
//        db.storeDevice(e1);
        
        mTxList = new ArrayList<IcdMsg>(5);
        mWaitingList = new ArrayList<DeviceUID>(5);
        mWaypointList = new ArrayList<String>();
        
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
        
        //if ( mIsLoggedIn){
		//	Intent devListIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
		//	devListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    startActivity(devListIntent);
		//}else{
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            mIsLoggedIn = true;
		//}
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }

	/**********************/
	/**********************/
	/**********************/


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
	

	/***
	 * Stops transmitting or receiving 802.15.4 messages
	 */
	public void logout() {
		stopRadioComm();
	}


	public void uploadData() {
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
			//return;
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
			//TODO send waypoint list
			//icdMsg = IcdMsg.buildIcdMsg(dev, MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SMAF);
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

		mTxList.add(icdMsg);
		updateWaitingList();
		destDev.txAscension++;
		db.storeDevice(destDev);
		
		//byte[] destAddrs = destDev.UID().getBytes();
		
//		if( !XbeeAPI.transmitPkt(destAddrs,icdMsg.getBytes()) )
//			notifyOfTransmissionResult(false,destUID); // if interface not available now
//		else{
//			short ackNo = (short) (destDev.txAscension & 0xFF);
//			Log.i(TAG,"Sent " + cmd.toString() + " , ACK: " + String.valueOf(ackNo));
//			addItemToTxMap(destDev.UID(), icdMsg);
//		}

		//destDev.pendingTxMsgCnt++;
		//destDev.txAscension++;
		//db.storeDevice(destDev);
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

//    public void onRadioTransmitResult(boolean result, DeviceUID destUID, short ackNo){
//		ComModule destDev = db.getDevice(destUID);
//    	if( destDev == null){
//    		Log.e(TAG,"Missing device record for " + destUID.toString());
//    		return;
//    	}
//
//    	IcdTxItem txItem = mIcdTxMap.get(destUID);
//    	if( txItem == null){
//    		//Log.e(TAG, "Missing tx item for " + destUID.toString());
//    		//notifyOfTransmissionResult(result,destUID);
//    		return;
//    	}
//    	
//    	short ackNoSent = (short)(txItem.msgSent.headerData.msgAsc & 0xFF);
//    	if( ackNoSent > ackNo ){
//    		//Log.e(TAG, "Received an invalid AckNo for " + destUID.toString() + ":" + String.valueOf(ackNo));
//    		return;
//    	}
//    	
//    	txItem.clearTimer();
//    	IcdMsg msgSent = txItem.msgSent;   	
//
//    	if( result == false){
//    		Log.i(TAG,"Timeout for ACK: " + String.valueOf(ackNo));
//	    	if( txItem.retryAttempts > 5){
//	    		toast("No reply received from " + destUID.toString() + "for AckNo: " + String.valueOf(ackNo));
//	    		notifyOfTransmissionResult(false,destUID);
//	    		mIcdTxMap.remove(destUID);
//	    	}else{
//	    		byte[] destAddrs = destUID.getBytes();
//	    		msgSent.headerData.msgAsc = destDev.txAscension++;
//	    		db.storeDevice(destDev);
//	    		XbeeAPI.transmitPkt(destAddrs, msgSent.getBytes()); 
//	    		txItem.msgSent = msgSent;
//	    		txItem.retryAttempts++;
//	    		txItem.restartTimer();
//	    	}
//    	}else{
//    		mIcdTxMap.remove(destUID);
//    	}
//    }
	
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
    			String key = msg.headerData.devUID.toString();
    			EcocDevice deviceSrc = (EcocDevice)db.getDevice(msg.headerData.devUID);
    	    	if( deviceSrc == null ){
    	    		deviceSrc = new EcocDevice( msg.headerData.devUID , frm.address);
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
     * @param uid
     */
    private void handleNullMsg(DeviceUID uid){
    	ArrayList<IcdMsg> removeList = new ArrayList<IcdMsg>();
    	for(IcdMsg msg : mTxList){
    		if( msg != null && msg.destUID.equals(uid)){
    			ComModule cm = this.getDeviceRecord(msg.destUID);
    			XbeeAPI.transmitPkt( cm.address ,msg.getBytes());
    			removeList.add(msg);
    		}
    	}
    	
    	if( removeList.size() > 0){
	    	for(IcdMsg msg : removeList){
	    		mTxList.remove(msg);
	    	}
	    	updateWaitingList();
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
    		db.storeDevice(deviceSrc);
    		ackNo = (short) (status.ackNo & 0xFF);
    		
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
    		
    		//onRadioTransmitResult(true,deviceSrc.devUID,ackNo);
    		sendDevCmd(deviceSrc.devUID,DeviceCommands.SEND_ACK);
    		Log.i(TAG,"Received record, ACK " + String.valueOf(ackNo));
    		toast("Rec Log " + String.valueOf(ackNo));
    		if( logRecord.eventType == EventLogType.END_OF_RECORDS){
    			Intent intent = new Intent(Events.DEV_EVENT_LOG_CHANGD);
    			intent.putExtra("deviceUID",deviceSrc.devUID);
    			sendBroadcast(intent);
    		}
    		break;
    	case NULL_MSG:
    		handleNullMsg(deviceSrc.devUID);
    		break;
    	case UNRESTRICTED_STATUS_MSG:
    		ackNo = (short) ( deviceSrc.rxAscension & 0xFF);
    		toast("Receiver status");
    		notifyOfTransmissionResult(true, deviceSrc.devUID);
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
		//return strToHex("1234567890ABCDEF1234567890ABCDEF"); 
	}

    /***************************/
    /***************************/
    /***************************/
    /***************************/
	
	
	public boolean isUsbAvalible(){
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
    
    public SharedPreferences getSettingsFile(){
		return settings;
	}
    
    /***
	 * Reload settings from the preferences file and confiure the ICD msg class
	 */
	protected void loadSettings(){
		boolean useEncryption = settings.getBoolean(SettingsKey.USE_ENC, false);
		
		String Android_ID = System.getString(this.getContentResolver(), System.ANDROID_ID);
		Android_ID = "0D0E0A0D0B0E0E0F";
		String thisUIDStr = settings.getString(SettingsKey.THIS_UID, Android_ID);
		//String dcpUIDStr = settings.getString(SettingsKey.DCP_UID, Android_ID);
		int burstIndex = settings.getInt(SettingsKey.NADA_BURST, 4);
		
		DeviceUID thisUID = new DeviceUID(Android_ID);
		DeviceUID dcpUID = new DeviceUID(Android_ID);
		
		IcdMsg.configure(useEncryption,DeviceType.FNAD_I, thisUID, this);	
		mNadaBroadcaster.config(burstIndex, DeviceType.INVALID, new DeviceUID("0000000000000000"), DeviceType.DCP, dcpUID);
		
		String temp =  settings.getString(SettingsKey.CONVEYANCE_ID, "ConveyanceID");;
		conveyanceID = new ConveyanceID(temp);
		String waypointStr = settings.getString(SettingsKey.WAYPOINT_LIST, "A4807.038N001131.000E,A4111.033N002222.001E");
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
        // Cancel the persistent notification.
    	toast("Service exit");
    	db.close();
        mNM.cancel(NEW_DEVICE_NOTIFICATION);

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
		// TODO Auto-generated method stub
		return mBinder;
	}
    
	private void showNewDeviceNotification(ComModule device) {       
		// In this sample, we'll use the same text for the ticker and the expanded notification        
		//TODO go to the device details for this new device
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + device.devUID;        // Set the icon, scrolling text and timestamp        
		Notification notification = new Notification(R.drawable.stat_sys_signal_4, text, java.lang.System.currentTimeMillis());        // The PendingIntent to launch our activity if the user selects this notification        
		Intent intent = new Intent(this, ECoCInfoActivity.class);
		intent.putExtra("deviceUID", device.devUID);
		intent.putExtra("clearNotifications",true);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);        // Set the info for the views that show in the notification panel.        
		notification.setLatestEventInfo(this, title,text, contentIntent);        // Send the notification. 
		notification.vibrate = new long[]{Notification.DEFAULT_VIBRATE};
		mNM.notify(NEW_DEVICE_NOTIFICATION, notification);    
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
		public static final String USERNAME = "USERNAME";
		public static final String PASSWORD = "PASSWORD";
		public static final String REMEMBER_PASS = "REMEMBER_PASS";
		
		public static final String THIS_UID = "THIS_UID";
		public static final String DCP_UID = "DCP_UID";
		public static final String DCP_ADDR = "DCP_ADDR";
		public static final String FTP_ADDR = "FTP_ADDR";
		public static final String USE_ENC = "USE_ENC";
		public static final String NADA_BURST = "NADA_BURST";
		
		public static final String CONVEYANCE_ID = "CONVEYANCE_ID";
		public static final String WAYPOINT_LIST = "WAYPOINT_LIST";
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
	
	public static final int NEW_DEVICE_NOTIFICATION = 0;

}
//startService(new Intent(this,UsbService.class));