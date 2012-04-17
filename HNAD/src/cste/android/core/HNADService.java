package cste.android.core;

import static cste.icd.general.Utility.strToHex;

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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import cste.android.R;
import cste.android.activities.DeviceListActivity;
import cste.android.activities.ECoCInfoActivity;
import cste.android.activities.LoginActivity;
import cste.android.db.DbHandler;
import cste.android.network.FtpHandler;
import cste.android.network.NetworkManager;
import cste.android.network.WebHandler;
import cste.android.network.WebHandler.CommandResult;
import cste.icd.components.ComModule;
import cste.icd.components.ECoC;
import cste.icd.general.KeyProvider;
import cste.icd.icd_messages.EventLogECM;
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
import cste.misc.HnadEventLog;
import cste.misc.XbeeFrame;

/***
 * HNAD background service
 * 
 * @author Sergio Enriquez
 * 
 */
public class HNADService extends Service implements KeyProvider {
	static final String TAG = "HNAD service";

	protected NADABroadcaster mNadaBroadcaster;
	protected IcdMessageHandler mIcdMessageHandler;
	protected UsbCommManager mUsbCommHandler;
	protected NetworkManager mNetworkHandler;
	protected NotificationManager mNotificationManager;
	protected SharedPreferences mSettings;
	protected Handler mNadaHandler;
	protected List<IcdMsg> mWaitingMsgList;
	protected List<IcdMsg> mSentMsgList;
	protected List<String> mWaypointList;
	protected Timer mUsbReconnectTimer;
	protected FtpHandler mFtpHandler;
	protected WebHandler mWebHandler;
	protected DbHandler db;
	protected final IBinder mBinder = new LocalBinder();

	protected int mWaypointIndex;
	protected boolean mIsLoggedIn;
	protected String mDcpUsername;
	protected String mDcpHostAddress;
	protected ConveyanceID mConveyanceID;// TODO use the custom class

	public void onCreate() {
		mNadaHandler = new Handler();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mIcdMessageHandler = new IcdMessageHandler(this);
		mNetworkHandler = new NetworkManager(mIcdMessageHandler);
		mUsbCommHandler = new UsbCommManager(this, mIcdMessageHandler);
		mNadaBroadcaster = new NADABroadcaster(this, mNadaHandler,
				mUsbCommHandler);
		mFtpHandler = new FtpHandler();
		mWebHandler = new WebHandler();

		mUsbReconnectTimer = null;
		XbeeAPI.setRadioInterface(mUsbCommHandler);
		XbeeAPI.setHnadService(this);

		mDcpUsername = "NA";
		db = new DbHandler(this);
		db.open();
		db.resetTempDeviceVars(); // clears rssi,visible,pendingTx vars
		db.storeHnadLog(NadEventLogType.POWER_ON, mDcpUsername);
		mIsLoggedIn = false;

//		db.storeDevice(new ECoC(new DeviceUID("1234567812345678"),
//				new byte[] {}));

		mSentMsgList = new ArrayList<IcdMsg>(5);
		mWaitingMsgList = new ArrayList<IcdMsg>(5);
		mWaypointList = new ArrayList<String>();
		mWaypointIndex = 0;

		mSettings = getSharedPreferences("PreferencesFile",
				Context.MODE_PRIVATE);
		loadSettings();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);

		if (mIsLoggedIn) {
			Intent devListIntent = new Intent(getApplicationContext(),
					DeviceListActivity.class);
			devListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(devListIntent);
		} else {
			Intent loginIntent = new Intent(getApplicationContext(),
					LoginActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(loginIntent);
		}
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	/**********************/
	/**********************/

	void processKeysFile(String fileContents) {
		String[] deviceInfoArray = fileContents.split("\r");
		for (int i = 0; i < deviceInfoArray.length; i++) {
			String[] lineContent = deviceInfoArray[i].split(",|\r");
			if (lineContent.length != 4)
				continue; // bad format

			String devUIDStr = lineContent[0];
			String devTypeCodeStr = lineContent[1];
			String devTCKstr = lineContent[2];
			String devTxAsc = lineContent[3];

			DeviceUID devUID = new DeviceUID(devUIDStr);
			DeviceType devType = DeviceType
					.fromValue(strToHex(devTypeCodeStr)[0]);
			byte[] devTCK = strToHex(devTCKstr);
			int ascension;
			if (devTxAsc == null || devTxAsc.length() == 0)
				ascension = 0;
			else
				ascension = Integer.parseInt(devTxAsc);

			if (!devUID.isValid())
				continue;// bad UID
			if (!devType.equals(DeviceType.ECOC))
				continue;// Onle ECoC is supported
			if (devTCK.length != 16)
				continue;// wrong key size
			if (ascension == 0)
				continue; // invalid ascension

			ECoC devRecord = new ECoC(devUID);
			devRecord.setTCK(devTCK);
			devRecord.txAscension = ascension;
			db.storeDevice(devRecord);
		}

	}

	public void login(String dcpUsername, String dcpPassword) {
		mIsLoggedIn = false;
		AuthenticationProcess task = new AuthenticationProcess();
		task.execute(new String[] { mDcpHostAddress, dcpUsername, dcpPassword });
	}

	// Stops transmitting or receiving 802.15.4 messages
	public void logout() {
		mIsLoggedIn = false;
		stopRadioComm();
	}

	public void setDeviceTCK(DeviceUID devUID, byte[] newTCK) {
		ComModule destDev = db.getDevice(devUID);

		if (destDev == null) {
			Log.e(TAG, "Tried to set TCK on a device that does not exist");
			return;
		}
		destDev.setTCK(newTCK);
		db.storeDevice(destDev);

		Intent intent = new Intent(Events.DEVICE_INFO_CHANGED);
		intent.putExtra("deviceUID", devUID);
		sendBroadcast(intent);
	}

	public void setDeviceAssensionVal(DeviceUID devUID, int val) {
		ComModule destDev = db.getDevice(devUID);
		if (destDev == null) {
			Log.e(TAG, "Tried to set assension on a device that does not exist");
			return;
		}
		destDev.txAscension = val;
		db.storeDevice(destDev);

		Intent intent = new Intent(Events.DEVICE_INFO_CHANGED);
		intent.putExtra("deviceUID", devUID);
		sendBroadcast(intent);
	}

	public void sendDevCmd(DeviceUID destUID, DeviceCommands cmd) {
		ComModule destDev = db.getDevice(destUID);
		if (destDev == null) {
			Log.w(TAG, destUID.toString() + " not stored, cannot send command");
			return;
		}

		if (!this.mUsbCommHandler.isReady()) {
			toast("Cannot transmit, USB not availible");
			notifyOfTransmissionResult(false, destUID);
			return;
		}

		IcdMsg icdMsg = null;
		switch (cmd) {
		case SEND_ACK:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.ACK, (byte) destDev.rxAscension);
			break;
		case SET_TIME:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.ST, IcdTimestamp.now());
			break;
		case SET_TRIPINFO:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.CWT, mConveyanceID);
			break;
		case SET_WAYPOINTS_START:
			if (this.mWaypointList.size() > 0) {
				mWaypointIndex = 0;
				GpsLoc gps = new GpsLoc(mWaypointList.get(mWaypointIndex++));
				icdMsg = IcdMsg.buildIcdMsg(destDev,
						MsgType.DEV_CMD_RESTRICTED, EcocCmdType.WLN, gps);
			} else {
				toast("No waypoints set");
				return;
			}
			break;
		case SET_WAYPOINTS_NEXT:
			if (mWaypointIndex < mWaypointList.size()) {
				GpsLoc gps = new GpsLoc(mWaypointList.get(mWaypointIndex++));
				icdMsg = IcdMsg.buildIcdMsg(destDev,
						MsgType.DEV_CMD_RESTRICTED, EcocCmdType.WLA, gps);
			} else {
				toast("Invalid waypoint index");
				return;
			}
			break;
		case SET_ALARM_OFF:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.SMAT);
			break;
		case SET_ALARM_ON:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.SMAF);
			break;
		case SET_COMMISION_OFF:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.DAHH);
			break;
		case SET_COMMISION_ON:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.SMAT);
			break;
		case GET_RESTRICTED_STATUS:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.NOP);
			break;
		case GET_UN_RESTRICTED_STATUS:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_UNRESTRICTED,
					UnrestrictedCmdType.REQUEST);
			break;
		case GET_EVENT_LOG:
			mWaypointIndex = 0;
			if (db.getDevLogRecordCount(destUID) == 0)
				icdMsg = IcdMsg.buildIcdMsg(destDev,
						MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SL);
			else
				icdMsg = IcdMsg.buildIcdMsg(destDev,
						MsgType.DEV_CMD_RESTRICTED, EcocCmdType.SLU);
			break;
		case CLEAR_EVENT_LOG:
			mWaypointIndex = 0;
			icdMsg = IcdMsg.buildIcdMsg(destDev, MsgType.DEV_CMD_RESTRICTED,
					EcocCmdType.EL);
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

	protected void notifyOfTransmissionResult(boolean success, DeviceUID destUID) {
		Intent intent = new Intent(Events.TRANSMISSION_RESULT);
		intent.putExtra("result", success);
		intent.putExtra("deviceUID", destUID);
		sendBroadcast(intent);
	}

	public void onFrameReceived(XbeeFrame frm) {
		if (frm.type == XbeeAPI.RX_64BIT) {
			IcdMsg msg = IcdMsg.fromBytes(frm.payload);
			if (msg.msgStatus == IcdMsg.MsgStatus.OK) {
				String key = msg.header.devUID.toString();
				ECoC deviceSrc = (ECoC) db.getDevice(msg.header.devUID);
				if (deviceSrc == null) {
					deviceSrc = new ECoC(msg.header.devUID, frm.address);
					db.storeDevice(deviceSrc);

					Intent intent = new Intent(Events.DEVLIST_CHANGED);
					intent.putExtra(Events.DEVLIST_CHANGED, key);
					sendBroadcast(intent);
					showNewDeviceNotification(deviceSrc);
				}
				deviceSrc.inRange = true;
				deviceSrc.rssi = (byte) frm.rssi;
				deviceSrc.rxAscension = msg.header.msgAsc;
				db.storeDevice(deviceSrc);
				handleIcdMsg(msg, deviceSrc);
			} else
				Log.w(TAG,
						"Received ICD msg with an error: "
								+ msg.msgStatus.name());
		}
	}

	private void onMsgResponseReceived(IcdMsg msgRec) {
		IcdMsg msgSent = null;
		for (IcdMsg msg : mSentMsgList) {
			if (msg != null && msg.destUID.equals(msgRec.header.devUID)) {
				// DB hnadlog store
				msgSent = msg;
				db.storeHnadLog(NadEventLogType.ICD_MSG_RECEIVED, mDcpUsername,
						msgSent, msgRec);
				break;// Tx only 1 msg at a time
			}
		}
		if (msgSent != null)
			mSentMsgList.remove(msgSent);
	}

	/***
	 * When NULL msg is received, transmit one buffered msg for the receiver
	 * 
	 * @param uid
	 */
	private void onNullMsgReceived(DeviceUID uid) {
		IcdMsg msgSent = null;
		for (IcdMsg msg : mWaitingMsgList) {
			if (msg != null && msg.destUID.equals(uid)) {
				ComModule cm = this.getDeviceRecord(msg.destUID);
				XbeeAPI.transmitPkt(cm.address, msg.getBytes());
				mSentMsgList.add(msg);
				msgSent = msg;
				break;// Tx only 1 msg at a time
			}
		}

		if (msgSent != null) {
			mWaitingMsgList.remove(msgSent);
			mNadaBroadcaster.updateWaitingList();
		}
	}

	/***
	 * 
	 * @param msg
	 */
	private void handleIcdMsg(IcdMsg msg, ComModule deviceSrc) {
		short ackNo;
		switch (msg.header.msgType) {
		case RESTRICTED_STATUS_MSG:
			RestrictedStatus status = (RestrictedStatus) msg.payload;
			deviceSrc.setRestrictedStatus((RestrictedStatus) msg.payload);
			db.storeDevice(deviceSrc);
			ackNo = (short) (status.ackNo & 0xFF);
			onMsgResponseReceived(msg);

			if (mWaypointIndex > 0) { // setting waypoints
				if (mWaypointIndex < mWaypointList.size()) {
					toast("Sending waypoint " + String.valueOf(mWaypointIndex));
					sendDevCmd(deviceSrc.devUID,
							DeviceCommands.SET_WAYPOINTS_NEXT);
				} else {
					toast("Done sending waypoints");
					mWaypointIndex = 0;
					notifyOfTransmissionResult(true, deviceSrc.devUID);
				}
			} else {
				notifyOfTransmissionResult(true, deviceSrc.devUID);
			}
			break;
		case DEVICE_EVENT_LOG:
			EventLogICD logRecord = (EventLogICD) msg.payload;
			ackNo = (short) (logRecord.ackNo & 0xFF);
			db.storeDevLog(deviceSrc.devUID, logRecord);
			onMsgResponseReceived(msg);

			sendDevCmd(deviceSrc.devUID, DeviceCommands.SEND_ACK);
			Log.i(TAG, "Received record, ACK " + String.valueOf(ackNo));
			toast("Rec Log " + String.valueOf(ackNo));
			if (logRecord.eventType == EcmEventLogType.END_OF_RECORDS) {
				Intent intent = new Intent(Events.ECM_EVENTLOG_CHANGE);
				intent.putExtra("deviceUID", deviceSrc.devUID);
				sendBroadcast(intent);
			}
			break;
		case NULL_MSG:
			onNullMsgReceived(deviceSrc.devUID);
			break;
		case UNRESTRICTED_STATUS_MSG:
			ackNo = (short) (deviceSrc.rxAscension & 0xFF);
			break;
		default:
			ackNo = (short) (deviceSrc.rxAscension & 0xFF);
		}

		Intent testIntent = new Intent(Events.DEVICE_INFO_CHANGED);
		testIntent.putExtra("deviceUID", deviceSrc.devUID);
		sendBroadcast(testIntent);
	}

	public ComModule getDeviceRecord(DeviceUID devUID) {
		return db.getDevice(devUID);
	}

	public void deleteDeviceRecord(DeviceUID devUID) {
		db.deleteDeviceRecord(devUID);
		Intent intent = new Intent(Events.DEVLIST_CHANGED);
		intent.putExtra("deviceUID", devUID);
		sendBroadcast(intent);
	}

	public void reloadSettings() {
		loadSettings();
	}

	/***************************/
	/***************************/

	public List<String> getWaypointList() {
		return mWaypointList;
	}

	public String getConveyanceIDStr() {
		return mConveyanceID.toString();
	}

	public List<IcdMsg> getTxList() {
		return mWaitingMsgList;
	}

	public Hashtable<DeviceUID, ComModule> getDeviceList() {
		return db.getStoredDevices();
	}

	public void deleteDeviceLogs(DeviceUID devUID) {
		db.deleteDevLogRecords(devUID);
	}

	public ArrayList<EventLogICD> getEcmEventLog(DeviceUID devUID) {
		return db.getDevLogRecords(devUID);
	}

	public ArrayList<HnadEventLog> getHnadEventLog() {
		return db.getHnadLogRecords();
	}

	public SharedPreferences getSettingsFile() {
		return mSettings;
	}

	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		ComModule cm = db.getDevice(destinationUID);
		if (cm == null) {
			Log.w(TAG, "No key availible for " + destinationUID.toString());
			return null;
		}
		if (cm.keyValid)
			return cm.getTCK();
		else
			return null;
	}

	public boolean getUsbState() {
		return this.mUsbCommHandler.isReady();
	}

	public void onUsbStateChanged(boolean connected) {
		Intent intent = new Intent(Events.USB_STATE_CHANGED);
		if (connected) {
			toast("USB is connected");
			mNadaHandler.removeCallbacks(mNadaBroadcaster);
			mNadaHandler.post(mNadaBroadcaster);
			intent.putExtra("usbState", true);
		} else {
			toast("USB was disconnected");
			mNadaHandler.removeCallbacks(mNadaBroadcaster);
			intent.putExtra("usbState", false);
		}
		sendBroadcast(intent);
	}

	/***
	 * Reload settings from the preferences file and confiure the ICD msg class
	 */
	protected void loadSettings() {
		boolean useEncryption = mSettings
				.getBoolean(SettingsKey.USE_ENC, false);
		String thisUIDStr = mSettings.getString(SettingsKey.THIS_UID,
				"0D0E0A0D0B0E0E0F");
		String dcpUIDStr = mSettings.getString(SettingsKey.DCP_UID,
				"0000000000000000");
		int burstIndex = mSettings.getInt(SettingsKey.NADA_BURST, 5);

		DeviceUID thisUID = new DeviceUID(thisUIDStr);
		DeviceUID dcpUID = new DeviceUID(thisUIDStr);

		IcdMsg.configure(useEncryption, DeviceType.FNAD_I, thisUID, this);
		mNadaBroadcaster.config(burstIndex, DeviceType.INVALID, new DeviceUID(
				dcpUIDStr), DeviceType.DCP, dcpUID);

		String temp = mSettings.getString(SettingsKey.CONVEYANCE_ID,
				"ConveyanceID");
		
		mDcpHostAddress = mSettings.getString(SettingsKey.AUTH_HOST_ADDR,
				"http://myhomeserver2.dyndns.org/dcp/Admin");
		
		mConveyanceID = new ConveyanceID(temp);
		
		//read the waypoints list
		String waypointStr = mSettings.getString(SettingsKey.WAYPOINT_LIST,
				""); // "A4807.038N001131.000E,A4111.033N002222.001E"

		if (waypointStr != "") {
			String[] waypointArr = waypointStr.split(",");
			mWaypointList = Arrays.asList(waypointArr);
		}

		String ftpHost = "127.0.0.1";
		String ftpPassword = "00000000000000000000000000000000";
		mFtpHandler.configureHost(ftpHost, "x" + thisUIDStr, ftpPassword, 21);
	}

	protected void stopRadioComm() {
		mNadaHandler.removeCallbacksAndMessages(mNadaBroadcaster);
		mUsbCommHandler.closeDevice();
		if (mUsbReconnectTimer != null)
			mUsbReconnectTimer.cancel();
	}

	@Override
	public void onDestroy() {
		toast("Service exit");
		db.storeHnadLog(NadEventLogType.POWER_OFF, mDcpUsername);
		db.close();
		mNotificationManager.cancel(Events.NEW_DEVICE_DETECTED); // Cancel the
																// persistent
																// notification.
		stopRadioComm();
	}

	private void toast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public Context getContext() {
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

	public void toggleDiscoveryMode(boolean state) {
		mNadaBroadcaster.setDeviceDiscoveryMode(state);
	}

	private void showNewDeviceNotification(ComModule device) {
		CharSequence title = "HNAD app";
		CharSequence text = "Device detected " + device.devUID; // Set the icon,
																// scrolling
																// text and
																// timestamp
		Notification notification = new Notification(
				R.drawable.stat_sys_signal_4, text,
				java.lang.System.currentTimeMillis()); // The PendingIntent to
														// launch our activity
														// if the user selects
														// this notification
		Intent intent = new Intent(this, ECoCInfoActivity.class);
		intent.putExtra("deviceUID", device.devUID);
		intent.putExtra("clearNotifications", true);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0); // Set the info for the views that show in the
							// notification panel.
		notification.setLatestEventInfo(this, title, text, contentIntent); // Send
																			// the
																			// notification.
		notification.vibrate = new long[] { Notification.DEFAULT_VIBRATE };
		mNotificationManager.notify(Events.NEW_DEVICE_DETECTED, notification);
	}

	public void saveWaypointSettings(String conveyanceStr,
			ArrayList<String> waypoints) {
		mConveyanceID = new ConveyanceID(conveyanceStr);
		mWaypointList = waypoints;

		SharedPreferences settings = getSettingsFile();
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(SettingsKey.CONVEYANCE_ID, conveyanceStr);
		StringBuilder sb = new StringBuilder("");
		for (String s : waypoints) {
			sb.append(s);
			sb.append(",");
		}
		editor.putString(SettingsKey.WAYPOINT_LIST, sb.toString());
		editor.commit();
	}

	public void saveFtpConfig(String hostAddrs, String username,String password, int port) {
		SharedPreferences settings = getSettingsFile();
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(SettingsKey.FTP_ADDR, hostAddrs);
		editor.putString(SettingsKey.FTP_USER, username);
		editor.putString(SettingsKey.FTP_PASS, password);
		editor.putInt(SettingsKey.FTP_PORT, port);

		editor.commit();
	}

	protected class AuthenticationProcess extends
			AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			Intent intent = new Intent(Events.LOGIN_RESULT);
			if (result.equals("success")) {
				mUsbReconnectTimer = new Timer("USB reconnect timer", true);
				mUsbReconnectTimer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						if (mUsbCommHandler.openDevice())
							this.cancel();
					}
				}, 100, 1000);// delay start 100ms, retry every second

				intent.putExtra("result", true);
				mIsLoggedIn = true;
				db.storeHnadLog(NadEventLogType.LOGIN_SUCCESS, mDcpUsername);
			} else {
				intent.putExtra("result", false);
				intent.putExtra("cause", result);
				db.storeHnadLog(NadEventLogType.LOGIN_FAILURE, mDcpUsername);
			}

			sendBroadcast(intent);
		}

		@Override
		protected String doInBackground(String... args) {

			if (args.length != 3)
				return "Bad paramter count";

			String hostUrl = args[0];
			String username = args[1];
			String password = args[2];

			String response = mWebHandler.authenticateUser(hostUrl, username,
					password);
			if (mWebHandler.lastError != CommandResult.SUCCESS) {
				if (mWebHandler.lastError == CommandResult.AUTHENTICATION)
					return "Invalid username or password";
				else
					return "Network Error";
			}

			String[] items = response.split(",");
			if (items.length >= 3) {
				String ftpHost = items[0];
				String ftpUsername = items[1];
				String ftpPassword = items[2];

				saveFtpConfig(ftpHost, ftpUsername, ftpPassword, 21);
				mFtpHandler
						.configureHost(ftpHost, ftpUsername, ftpPassword, 21);
			} else
				return "HTTP response parse error";

			String keyFile = mFtpHandler.getKeysFileContent();
			if (keyFile.equals("")) {
				return "Could not retrieve device keys from FTP";
			} else
				processKeysFile(keyFile);

			return "success";
		}
	}

	public class SettingsKey {
		public static final String DCP_USERNAME = "USERNAME";
		public static final String DCP_PASSWORD = "PASSWORD";
		public static final String REMEMBER_PASS = "REMEMBER_PASS";

		public static final String THIS_UID = "THIS_UID";
		public static final String DCP_UID = "DCP_UID";
		public static final String AUTH_HOST_ADDR = "AUTH_HOST_ADDR";
		
		public static final String DCP_ADDR = "DCP_ADDR";
		public static final String DCP_PORT = "DCP_PORT";

		public static final String FTP_ADDR = "FTP_ADDR";
		public static final String FTP_USER = "FTP_ADDR";
		public static final String FTP_PASS = "FTP_ADDR";
		public static final String FTP_PORT = "FTP_ADDR";

		public static final String USE_ENC = "USE_ENC";
		public static final String NADA_BURST = "NADA_BURST";

		public static final String CONVEYANCE_ID = "CONVEYANCE_ID";
		public static final String WAYPOINT_LIST = "WAYPOINT_LIST";
	}

	public class Events {
		public static final String DEVICE_INFO_CHANGED = "DEVICE_INFO_CHANGED";
		public static final String DEVLIST_CHANGED = "DEVLIST_CHANGED";
		public static final String LOGIN_RESULT = "LOGIN_RESULT";
		public static final String UPLOAD_DATA = "UPLOAD_DATA";
		public static final String USB_STATE_CHANGED = "USB CONNECTED";
		public static final String TRANSMISSION_RESULT = "USB TRANSMISSION_RESULT";
		public static final String ECM_EVENTLOG_CHANGE = "USB TRANSMISSION_RESULT";
		public static final String HNAD_EVENTLOG_CHANGE = "HNAD_EVENT_LOG_CHANGD";
		
		public static final int    NEW_DEVICE_DETECTED = 0;
	}

	public enum DeviceCommands {
		SEND_ACK, SET_TIME, SET_TRIPINFO, SET_WAYPOINTS_START, SET_WAYPOINTS_NEXT, SET_ALARM_OFF, SET_ALARM_ON, SET_COMMISION_OFF, SET_COMMISION_ON, GET_RESTRICTED_STATUS, GET_UN_RESTRICTED_STATUS, GET_EVENT_LOG, CLEAR_EVENT_LOG
	}
}
// startService(new Intent(this,UsbService.class));