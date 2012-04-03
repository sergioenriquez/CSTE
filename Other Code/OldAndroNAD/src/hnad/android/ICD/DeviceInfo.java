package hnad.android.ICD;

import hnad.android.R;
import hnad.android.ListAdapter.ImageArrayAdapterItem;
import hnad.android.ListAdapter.TwoLineArrayAdapterItem;

import java.util.ArrayList;

import android.os.CountDownTimer;

/**
 * Wrapper containing the info related to a device.
 * 
 * @author Cory Sohrakoff
 *
 */
public class DeviceInfo implements TwoLineArrayAdapterItem, ImageArrayAdapterItem {		
	protected static final byte ALARM_NO			= (byte) 0x00;	
	protected static final byte ALARM_YES			= (byte) 0x01;
	
	protected static final byte DOOR_CLOSED			= (byte) 0x00;
	protected static final byte DOOR_OPEN			= (byte) 0x01;

	protected static final byte OP_MODE_DISARMED	= (byte) 0x00;
	protected static final byte OP_MODE_ARMED		= (byte) 0x01;
	
	// Constants here for device info
	private static final byte DEVICE_TYPE_ACSD	= (byte) 0x80;
	
	private static final String DEVICE_TYPE_NAME_ACSD 		= "ACSD Type 0";
	
	/**
	 * per ICD ascension numbers begin with 1
	 */
	protected static final int ASCENSION_INITIAL_VALUE	= 1;
	
	// final so you can't change the UID
	private   final String mUID;
	
	private	  byte		mDeviceType;
	protected byte		mMessageType;
	protected byte		mRestrictedErrorSection;
	private	  byte		mDeviceOperatingMode;
	protected byte		mSensorOperatingMode;
	protected byte		mRestrictedStatusBits;
	protected byte		mSensorErrorBits;
	private	  String	mMechanicalSealId;
	private	  String	mConveyanceId;
	private   String 	mManifest;
	private	  byte		mAlarmStatus;
	private	  byte		mDoorStatus;
	protected int		mReceiveAscensionRestricted; // header ascension is 4 bytes so an int works
	protected int		mSendAscensionRestricted;
	protected byte[] 	mLastSentACK;
	
	protected boolean	mCommandRunning;
	protected boolean	mWaitingForLog;
	protected byte[]	mLastSentCommand;
	protected int		mCommandRetryCount;
	protected int		mACKRetryCount;
	
	protected byte		mLastSentCommandType;
	protected byte		mLastSentCommandOpcode;
	
	protected byte		mEventLogCommandAscension;
	private ArrayList<String> mEventLog;
	
	// Timer to go signal resending a message.
	private CountDownTimer mRetryTimer;
	
	/**
	 * Cancels the current timer and starts the new one.
	 * @param timer
	 */
	protected synchronized void setRetryTimer(CountDownTimer timer) {
		cancelRetryTimer();
		mRetryTimer = timer;
		mRetryTimer.start();
	}
	
	protected synchronized void addLogEntry(String logEntry) {
		mEventLog.add(logEntry);
	}
	
	public synchronized void clearLog() {
		mEventLog.clear();
	}
	
	public synchronized ArrayList<String> getLog() {
		return new ArrayList<String>(mEventLog);
	}
	
	/**
	 * Cancels the current timer.
	 */
	public synchronized void cancelRetryTimer() {
		if (mRetryTimer != null) {
			mRetryTimer.cancel();
			mRetryTimer = null;
		}
	}
	
	/**
	 * Get the device type name from the device type bits.
	 * 
	 * TODO add all device type names here, for now it will just show type number if not ACSD
	 * 
	 * @return
	 */
	public String getDeviceTypeString() {
		switch (getDeviceType()) {
		case DEVICE_TYPE_ACSD:
			return DEVICE_TYPE_NAME_ACSD;
		default:
			return String.format("0x%02X", getDeviceType());
		}
	}

	/**
	 * Create an object containing information about a device.
	 * 
	 * @param deviceType
	 * @param messageType
	 * @param uid
	 * @param restrictedErrorSection
	 * @param deviceOperatingMode
	 * @param sensorOperatingMode
	 * @param restrictedStatusBits
	 * @param sensorErrorBits
	 * @param mechanicalSealId
	 * @param conveyanceId
	 * @param manifest
	 * @param alarmStatus
	 * @param doorStatus
	 * @param inAscensionRestricted
	 */
	public DeviceInfo(byte deviceType, byte messageType, String uid,
			byte restrictedErrorSection, byte deviceOperatingMode,
			byte sensorOperatingMode, byte restrictedStatusBits,
			byte sensorErrorBits, String mechanicalSealId,
			String conveyanceId, String manifest, byte alarmStatus,
			byte doorStatus, int inAscensionRestricted) {
		this.mDeviceType = deviceType;
		this.mMessageType = messageType;
		this.mUID = uid;
		this.mRestrictedErrorSection = restrictedErrorSection;
		this.mDeviceOperatingMode = deviceOperatingMode;
		this.mSensorOperatingMode = sensorOperatingMode;
		this.mRestrictedStatusBits = restrictedStatusBits;
		this.mSensorErrorBits = sensorErrorBits;
		this.mMechanicalSealId = mechanicalSealId;
		this.mConveyanceId = conveyanceId;
		this.mManifest = manifest;
		this.mAlarmStatus = alarmStatus;
		this.mDoorStatus = doorStatus;
		this.mReceiveAscensionRestricted = inAscensionRestricted;
		
		// Initialize outgoing message info.
		this.mSendAscensionRestricted 	= ASCENSION_INITIAL_VALUE;
		this.mCommandRunning			= false;
		this.mWaitingForLog				= false;
		this.mLastSentCommand			= null;
		this.mLastSentACK 				= null;
		
		// initialize event log
		mEventLog = new ArrayList<String>();
	}
	

	@Override
	public String line1() {
		return "UID: " + getUID();
	}

	@Override
	public String line2() {
		return "Device: " + getDeviceTypeString();
	}
	
	@Override
	public int getImageResource() {
		if (alarm())
			return R.drawable.alarm;
		else if (armed())
			return R.drawable.armed;
		else
			return R.drawable.disarmed;
	}

	@Override
	public boolean equals(Object o) {
		DeviceInfo device = (DeviceInfo) o;
		return this.getUID().equals(device.getUID());
	}

	@Override
	public int hashCode() {
		return mUID.hashCode();
	}
	
	public String getUID() {
		return mUID;
	}

	/**
	 * Whether or not the device is armed.
	 * 
	 * @return
	 */
	public synchronized boolean armed() {
		return (mDeviceOperatingMode == OP_MODE_ARMED);
	}

	/**
	 * Whether or not the door is open.
	 * 
	 * @return
	 */
	public synchronized boolean doorOpen() {
		return (mDoorStatus == DOOR_OPEN);
	}

	/**
	 * Whether or not the alarm is going off.
	 * 
	 * @return
	 */
	public synchronized boolean alarm() {
		return (mAlarmStatus == ALARM_YES);
	}

	public synchronized int getDeviceType() {
		return mDeviceType;
	}	

	public synchronized String getMechanicalSealId() {
		return mMechanicalSealId;
	}

	public synchronized String getConveyanceId() {
		return mConveyanceId;
	}

	public synchronized String getManifest() {
		return mManifest;
	}
	
	

	public synchronized void setDeviceType(byte deviceType) {
		this.mDeviceType = deviceType;
	}

	public synchronized void setDeviceOperatingMode(byte deviceOperatingMode) {
		this.mDeviceOperatingMode = deviceOperatingMode;
	}

	public synchronized void setMechanicalSealId(String mechanicalSealId) {
		this.mMechanicalSealId = mechanicalSealId;
	}

	public synchronized void setManifest(String manifest) {
		this.mManifest = manifest;
	}

	public synchronized void setAlarmStatus(byte alarmStatus) {
		this.mAlarmStatus = alarmStatus;
	}

	public synchronized void setDoorStatus(byte doorStatus) {
		this.mDoorStatus = doorStatus;
	}
	
	public synchronized void setConveyanceId(String conveyanceId) {
		this.mConveyanceId = conveyanceId;
	}

	/**
	 * Return String containing detailed device information.
	 * 
	 */
	@Override
	public String toString() {
		return("       UID:  "   + getUID() + 
			 "\n    Device:  " + getDeviceTypeString() + 
			 "\n     Armed:  " + (armed() ? "YES" : "NO") + 
			 "\n      Door:  " + (doorOpen() ? "OPEN" : "CLOSED") + 
			 "\n     Alarm:  " + (alarm() ? "YES" : "NO")) +
			 "\n      Seal:  " + getMechanicalSealId().trim() +
			 "\n  Manifest:  " + getManifest().trim() +
			 "\nConveyance:  " + getConveyanceId().trim();
	}
	
	
}

