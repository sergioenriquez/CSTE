package hnad.android.ICD;

import hnad.android.R;
import hnad.android.Miscellaneous.Utils;
import hnad.android.Service.AndroNadService;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.TimeZone;

import org.bouncycastle2.crypto.engines.AESEngine;
import org.bouncycastle2.crypto.modes.CCMBlockCipher;
import org.bouncycastle2.crypto.params.CCMParameters;
import org.bouncycastle2.crypto.params.KeyParameter;
import org.bouncycastle2.openssl.EncryptionException;
import org.bouncycastle2.util.encoders.Hex;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * This class implements ICD version 8 for security devices. We are implementing an HNAD. However,
 * since this is modeled after DC-Lite, it contains some DCP commands, which can be changed later.
 * 
 * This implementation discards command messages after 5 unsuccessful retry attempts.
 * 
 * @author Cory Sohrakoff
 *
 */
public class ICD {	
	// For debugging
	private static final String TAG = ICD.class.getName();
	private static final boolean D = true;
	
	// discard ICD messages if the Rev Number is not the same as this ICD implementation
	private static final boolean DISCARD_WRONG_VERSION = true;
	
	// use encryption where required
	private boolean mUseEncryption = true;
	
	public synchronized boolean useEncryption() {
		return mUseEncryption;
	}

	public synchronized void setEncryption(boolean useEncryption) {
		this.mUseEncryption = useEncryption;
		if (D) Log.i(TAG, "Encryption: " + (useEncryption ? "ON" : "OFF"));
	}

	// use unrestricted checksum
	private static final boolean USE_UNRESTRICTED_CHECKSUM 	= false;
	
	// Message encryption key length
	private static final int ENCRYPTION_KEY_LENGTH			= 16;
	public  static final int ENCRYPTION_KEY_LENGTH_IN_HEX	= ENCRYPTION_KEY_LENGTH * 2;
	
	/**
	 * Default encryption key for xCSD in lab.
	 */
	private String mEncryptionKey = "da486be78ab8830e10d657f66ea7d8db";
	
	public synchronized String getEncryptionKey() {
		return mEncryptionKey;
	}

	public synchronized void setEncryptionKey(String encryptionKey) {
		this.mEncryptionKey = encryptionKey;
		if (D) Log.i(TAG, "Using encryption key: " + encryptionKey);
	}

	// This device type
	private static final byte DEVICE_TYPE 					= (byte)0x88; // TODO set device type: 0x88 = DCP, 0x86 = HNAD
	
	private static final boolean USE_xCSD_COMPATIBILITY = true;
	
	// The ICD revision number this implements
	private static final byte ICD_REV_NUMBER 				= (USE_xCSD_COMPATIBILITY ? 1 : 2);
	
	// Constants for resending command when no ACK is received
	private static final int COMMAND_RETRY_ATTEMPTS			= 5; // max number of command retries
	private static final int ACK_RETRY_ATTEMPTS				= 5; // max number of ACK retries
	private static final int COMMAND_TIMEOUT				= 20 * 1000; // 20 seconds
	
	// This device's UID
	private static final String DEFAULT_DEVICE_UID = "0123456789ABCDEF";
	
	// Conveyance ID format is ISO 6346
	private static final byte	CONVEYANCE_ID_FORMAT		=  0; // 0x00 is ISO 6346
	
	// UTC Length per ICD
	private static final int UTC_LENGTH						= 8; // bytes
	
	// UTC offsets
	private static final int UTC_MONTH						= 0; // month is 1-12
	private static final int UTC_DAY_OF_MONTH				= 1; // day is 1-31
	private static final int UTC_YEARS_SINCE_2000			= 2;
	private static final int UTC_DAY_OF_WEEK				= 3; // day is 0-6
	private static final int UTC_HOURS_SINCE_MIDNIGHT		= 4; // day is 0-23
	private static final int UTC_MINUTES_AFTER_HOUR			= 5; // day is 0-59
	private static final int UTC_SECONDS_AFTER_MINUTE		= 6; // day is 0-61
	private static final int UTC_FRACTIONAL_SECONDS			= 7;
	
	
	
	// Message integrity check (MIC) length
	private static final int MIC_LENGTH 					=  8; // in bytes
	public  static final int UID_LENGTH						=  8; // in bytes
	public	static final int UID_HEX_LENGTH					= 16; // in hex chars
	
	// Message header byte offsets
	private static final int MH_DEVICE_TYPE 				=  0;
	private static final int MH_MESSAGE_TYPE 				=  1;
	private static final int MH_MESSAGE_LENGTH				=  2;
	private static final int MH_DEVICE_UID					=  3;
	private static final int MH_ICD_REV_NUMBER				= 11;
	private static final int MH_MESSAGE_ASCENSION_NUMBER	= 12;
	
	// Message header lengths
	private static final int MH_ASCENSION_LENGTH			=  4;
	private static final int MH_HEADER_LENGTH 				= 16;
	
	// Message types
	private static final byte MESSAGE_UNRESTRICTED_STATUS 	= (byte) 0x00;
	private static final byte MESSAGE_RESTRICTED_STATUS 	= (byte) 0x80;
	private static final byte MESSAGE_EVENT_LOG_RECORD		= (byte) 0x81;
	private static final byte MESSAGE_UNRESTRICTED_COMMAND	= (byte) 0xC0;
	private static final byte MESSAGE_RESTRICTED_COMMAND	= (byte) 0xC1;
	
	// Unrestricted status message byte offsets
	private static final int USM_DEVICE_SECTION				= 0 + MH_HEADER_LENGTH;
	private static final int USM_ERROR_SECTION				= 1 + MH_HEADER_LENGTH;
	
	// Bit mask for unrestricted device section
	private static final byte DEVICE_SECTION_UNSOLICITED	= (byte) 0x2;
	
	// Unrestricted status message section lengths
	private static final int USM_DEVICE_SECTION_LENGTH		=  1;
	private static final int USM_ERROR_SECTION_LENGTH		= 14; // at least this many bytes
	
	// Unrestricted status message total length
	private static final int USM_TOTAL_LENGTH				= USM_DEVICE_SECTION_LENGTH +
															  USM_ERROR_SECTION_LENGTH; // at least this long
	
	
	// Restricted status message byte offsets
	private static final int RSM_RESTRICTED_ERROR_SECTION	=  0 + MH_HEADER_LENGTH;
	
	// Restricted error section length
	private static final int RSM_RESTRICTED_ERROR_LENGTH	=  1;
	
	private static final int RSM_ACK_ASCENSION_NUMBER		=  0 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_DEVICE_OPERATING_MODE		=  1 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_SENSOR_OPERATING_MODE		=  2 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_RESTRICTED_STATUS_BITS		=  3 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_RESTRICTED_ERROR_BITS		=  4 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_SENSOR_ERROR_BITS			=  5 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_MECHANICAL_SEAL_ID			=  6 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_CONVEYANCE_ID				= 21 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_MANIFEST					= 37 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_ALARM_STATUS				= 53 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	private static final int RSM_DOOR_STATUS				= 54 + MH_HEADER_LENGTH + RSM_RESTRICTED_ERROR_LENGTH;
	
	// Restricted error section bit masks
	private static final byte RESTRICTED_ERROR_UNSOLICITED 	= (byte) 0x2;
	
	// Restricted data section lengths (if more than one byte)
	private static final int MECHANICAL_SEAL_ID_LENGTH		= 15;
	private static final int CONVEYANCE_ID_LENGTH			= 16; // id itself is 15 since format takes 1 byte
	private static final int MANIFEST_LENGTH				= 16;
	private static final int RSM_RESTRICTED_DATA_LENGTH 	= 55;
	private static final int RSM_RESERVED_LENGTH			= 16;
	
	// Restricted status message total length
	private static final int RSM_TOTAL_LENGTH				= RSM_RESTRICTED_ERROR_LENGTH + 
															  RSM_RESTRICTED_DATA_LENGTH +
															  RSM_RESERVED_LENGTH +
															  MIC_LENGTH;
	
	// Event log message byte offsets
	private static final int EL_LOG_REQUEST_ACK_NUMBER		= 0 + MH_HEADER_LENGTH;
	private static final int EL_EVENT_TYPE					= 1 + MH_HEADER_LENGTH;
	private static final int EL_EVENT_TIME					= 2 + MH_HEADER_LENGTH;
	private static final int EL_DEVICE_STATUS_SECTION		= EL_EVENT_TIME + UTC_LENGTH;
	private static final int EL_DEVICE_OPERATING_MODE		= 0 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_RESTRICTED_STATUS_BITS		= 1 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_RESTRICTED_ERROR_BITS		= 2 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_ALARM_STATUS				= 3 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_CONVEYANCE_ID				= 4 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_DOOR_STATUS					= 20 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_COMMAND_MESSAGE_TYPE		= 21 + EL_DEVICE_STATUS_SECTION;
	private static final int EL_COMMAND_OPCODE				= 22 + EL_DEVICE_STATUS_SECTION;
	
	// Event log message lengths
	private static final int EL_ENCRYPTED_SECTION_LENGTH	= 33; // bytes
	private static final int EVENT_LOG_TOTAL_LENGTH			= EL_ENCRYPTED_SECTION_LENGTH +
															  MIC_LENGTH;
	
	// Event log event types
	private static final byte EVENT_END_OF_RECORDS			= (byte) 0xff;
	private static final byte EVENT_CHANGED_NAD				= (byte) 0x00;
	private static final byte EVENT_STATUS_CHANGE			= (byte) 0x01;
	private static final byte EVENT_STATUS_CHANGE_COMMAND	= (byte) 0x02;
	private static final byte EVENT_ALARM_CHANGE			= (byte) 0x03;
	private static final byte EVENT_FAULT_DETECTION			= (byte) 0x04;
	
	// Restricted command message offsets
	private static final int RCMD_RESTRICTED_SECTION		= MH_HEADER_LENGTH;
	private static final int RCMD_OPCODE					= RCMD_RESTRICTED_SECTION; // first byte in restricted section
	
	// Restricted command opcodes
	private static final byte RCMD_OPCODE_ACK				= (byte) 0x01;
	private static final byte RCMD_OPCODE_NOP				= (byte) 0x00;
	private static final byte RCMD_OPCODE_SMAT				= (byte) 0xA1;
	private static final byte RCMD_OPCODE_SMAF				= (byte) 0xA2;
	private static final byte RCMD_OPCODE_DADC				= (byte) 0x80;
	private static final byte RCMD_OPCODE_ST				= (byte) 0x03;
	private static final byte RCMD_OPCODE_SIS				= (byte) 0x02;
	public 	static final byte RCMD_OPCODE_ARMT				= (byte) 0x04;
	public 	static final byte RCMD_OPCODE_CTI				= (byte) 0x05;
	public 	static final byte RCMD_OPCODE_SL				= (byte) 0xA4;
	public 	static final byte RCMD_OPCODE_SLU				= (byte) 0xA3;
	public 	static final byte RCMD_OPCODE_EL				= (byte) 0xA5;
	
	// restricted command lengths
	private static final byte RCMD_TIME_LENGTH				= 8; // bytes
	
	// unrestricted command message offsets
	private static final int UCMD_UNRESTRICTED_SECTION		= MH_HEADER_LENGTH;
	private static final int UCMD_OPCODE					= UCMD_UNRESTRICTED_SECTION; // first byte of unrestricted section
	
	// Lengths
	private static final int UCMD_CHECKSUM_LENGTH			= 1; // in bytes
	
	// Unrestricted command opcodes
	private static final byte UCMD_OPCODE_USTATUS			= (byte) 0x00;
	
	
	// This device's UID
	private String mDeviceUID;
	
	private AndroNadService mAndroNadService;
	
	public ICD(AndroNadService service, String deviceUID) {
		mAndroNadService = service;
		// get UID or set to default
		if (deviceUID == null || deviceUID.length() != UID_HEX_LENGTH) // also should check that it is in hex
			mDeviceUID = DEFAULT_DEVICE_UID;
		else
			mDeviceUID = deviceUID;
		
		if (D) Log.i(TAG, "ICD running with UID: " + mDeviceUID);
	}
	
	/**
	 * Parse message header and then parse the rest of the message based on type.
	 * 
	 * @param message
	 * @param length
	 */
	public void parseMessage(byte message[], int length) {
		if (length < MH_HEADER_LENGTH) {
			Log.e(TAG, "Message too short to contain header. Discarding...");
			return;
		}
		
		// Get the message header.
		MessageHeader header = new MessageHeader();
		header.deviceType = message[MH_DEVICE_TYPE];
		header.messageType = message[MH_MESSAGE_TYPE];
		header.messageLength = message[MH_MESSAGE_LENGTH];
		header.deviceUID = Utils.bytesToHexString(message, MH_DEVICE_UID, UID_LENGTH);
		header.icdRevNumber = message[MH_ICD_REV_NUMBER];
		header.messageAscensionNumber = Utils.bytesToInt(message, MH_MESSAGE_ASCENSION_NUMBER);
		// Since ascension is only 4 bytes we'll use an int, if we need bigger we can just
		// store it as bytes using Arrays.copyOfRange().
		
		// check if message complies with our ICD Rev Number
		if (header.icdRevNumber != ICD_REV_NUMBER && DISCARD_WRONG_VERSION) {
			Log.e(TAG, "Message is not ICD Rev " + String.format("0x%02X", ICD_REV_NUMBER) + ". Discarding...");
			return;
		}
		
		if (D) Log.d(TAG, header.toString());
		if (D) Log.d(TAG, Utils.bytesToHexString(message, 0, length));
		
		// if ascension is bad drop the message
		if (header.messageAscensionNumber < DeviceInfo.ASCENSION_INITIAL_VALUE) {
			Log.e(TAG, "Received invalid ascension number: " + header.messageAscensionNumber + " is less than " +
					"the initial value (" + DeviceInfo.ASCENSION_INITIAL_VALUE +  ") defined in the ICD");
			return;
		}
		
		// parse rest of message based on message type.
		switch (header.messageType) {
		case MESSAGE_RESTRICTED_STATUS:
			if (!USE_xCSD_COMPATIBILITY && length == MH_HEADER_LENGTH + RSM_TOTAL_LENGTH)
				parseRestrictedStatus(message, length, header);
			else if (USE_xCSD_COMPATIBILITY && length >= MH_HEADER_LENGTH + RSM_TOTAL_LENGTH)
				parseRestrictedStatus(message, length, header); // parsing is the same for now
			else
				Log.e(TAG, "Restricted status message received, but length is incorrect: " + length + " bytes (including header)");
			break;
		case MESSAGE_UNRESTRICTED_STATUS:
			if (length >= MH_HEADER_LENGTH + USM_TOTAL_LENGTH)
				parseUnrestrictedStatus(message, length, header);
			else
				Log.e(TAG, "Unrestricted status message received, but length is incorrect: " + length + " bytes (including header)");
			break;
		case MESSAGE_EVENT_LOG_RECORD:
			if (!USE_xCSD_COMPATIBILITY && length == MH_HEADER_LENGTH + EVENT_LOG_TOTAL_LENGTH)
				parseEventLogRecord(message, length, header);
			if (USE_xCSD_COMPATIBILITY && length >= MH_HEADER_LENGTH + EVENT_LOG_TOTAL_LENGTH)
				parseEventLogRecord(message, length, header);
			else
				Log.e(TAG, "Log message received, but length is incorrect: " + length + " bytes (including header)");
			break;
		default:
			Log.e(TAG, "Unknown message type: " + Utils.bytesToHexString(message, 0, length));
			break;
		}
	}
	
	/**
	 * Encrypt the encrypted section of a message.
	 * 
	 * @param message
	 */
	private void encrypt(byte[] message) throws Exception {
		byte[] key = Hex.decode(getEncryptionKey());
		byte[] nonce = new byte[MH_HEADER_LENGTH - UID_LENGTH]; // nonce is just header without UID
		// copy nonce
		System.arraycopy(message, MH_DEVICE_TYPE, nonce, 0, 3);
		System.arraycopy(message, MH_ICD_REV_NUMBER, nonce, 3, 5);
		byte[] encrypted;
		int encLen = 0;
		
		CCMBlockCipher cipher = new CCMBlockCipher(new AESEngine());
		cipher.init(true, new CCMParameters(new KeyParameter(key), MIC_LENGTH * 8, nonce, null));
		try {
			encrypted = new byte[message.length - MH_HEADER_LENGTH];
				//cipher.processPacket(message, MH_HEADER_LENGTH, message.length - MH_HEADER_LENGTH - MIC_LENGTH);
			encLen += cipher.processBytes(message, MH_HEADER_LENGTH, encrypted.length - MIC_LENGTH, encrypted, 0);
			encLen += cipher.doFinal(encrypted, encLen);
			if (encLen != message.length - MH_HEADER_LENGTH)
				throw new EncryptionException("Length of encrypted message not what expected");
			// copy encrypted bytes to message
			System.arraycopy(encrypted, 0, message, MH_HEADER_LENGTH, encrypted.length);
		} catch (Exception e) {
			Log.e(TAG, "Encryption failure", e);
			// pass up exception to notify that encryption failed.
			throw e;
		}
	}
	
	/**
	 * Decrypt the encrypted section of a message.
	 * 
	 * @param message
	 */
	private void decrypt(byte[] message) throws Exception {
		byte[] key = Hex.decode(getEncryptionKey());
		byte[] nonce = new byte[MH_HEADER_LENGTH - UID_LENGTH];
		// copy nonce
		System.arraycopy(message, MH_DEVICE_TYPE, nonce, 0, 3);
		System.arraycopy(message, MH_ICD_REV_NUMBER, nonce, 3, 5);
		byte[] decrypted;
		int decLen = 0;
		
		CCMBlockCipher cipher = new CCMBlockCipher(new AESEngine());
		cipher.init(false, new CCMParameters(new KeyParameter(key), MIC_LENGTH * 8, nonce, null));
		try {
			decrypted = new byte[message.length - MH_HEADER_LENGTH - MIC_LENGTH];
			decLen += cipher.processBytes(message, MH_HEADER_LENGTH, message.length - MH_HEADER_LENGTH, decrypted, 0);
			decLen += cipher.doFinal(decrypted, decLen);
			if (decrypted.length != message.length - MIC_LENGTH - MH_HEADER_LENGTH)
				throw new EncryptionException("Length of decrypted message not what expected");
			// copy decrypted bytes to message
			System.arraycopy(decrypted, 0, message, MH_HEADER_LENGTH, decrypted.length);
		} catch (Exception e) {
			Log.e(TAG, "Decryption failure", e);
			// pass up exception to notify that encryption failed.
			throw e;
		}
	}
	
	/**
	 * Parse (and decrypt) a restricted status message.
	 * 
	 * @param message
	 * @param length
	 * @param header
	 */
	private void parseRestrictedStatus(byte[] message, int length, MessageHeader header) {
		if (D) Log.d(TAG, "Restricted status message received.");
		
		if (useEncryption()) {
			try {
				decrypt(message);
			} catch (Exception e) {
				Log.e(TAG, "RSTATUS message decryption failed. Discarding...");
				return;
			}
		}
		
		byte	restrictedErrorSection 	= message[RSM_RESTRICTED_ERROR_SECTION];
		byte	deviceOperatingMode		= message[RSM_DEVICE_OPERATING_MODE];
		byte	sensorOperatingMode		= message[RSM_SENSOR_OPERATING_MODE];
		byte	restrictedStatusBits	= message[RSM_RESTRICTED_STATUS_BITS];
		byte	sensorErrorBits			= message[RSM_SENSOR_ERROR_BITS];
		String	mechanicalSealId;
		String	conveyanceId;			
		String 	manifest;
		try {
			mechanicalSealId			= new String(message, RSM_MECHANICAL_SEAL_ID, MECHANICAL_SEAL_ID_LENGTH, "US-ASCII");
			// + 1 and - 1 because we are ignoring the first byte of conveyance since there is only one container type right now.
			conveyanceId				= new String(message, RSM_CONVEYANCE_ID + 1, CONVEYANCE_ID_LENGTH - 1, "US-ASCII");
			manifest					= new String(message, RSM_MANIFEST, MANIFEST_LENGTH, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error parsing restricted status message. Discarding...", e);
			return;
		}
		byte	alarmStatus				= message[RSM_ALARM_STATUS];
		byte	doorStatus				= message[RSM_DOOR_STATUS];
		
		boolean messageValid = true;
		DeviceInfo device = mAndroNadService.getDeviceInfo(header.deviceUID);
		if (device != null) {
			synchronized (device) {
				// check ascension number
				if (header.messageAscensionNumber < device.mReceiveAscensionRestricted) {
					// drop message
					if (D) Log.i(TAG, "Invalid ascension number...dropping message");
					messageValid = false;
				} else if (header.messageAscensionNumber == device.mReceiveAscensionRestricted) {
					messageValid = false;
					if (device.mACKRetryCount < ACK_RETRY_ATTEMPTS) {
						// resend ack
						device.mACKRetryCount++;
						if (D) Log.i(TAG, "Duplicate ascension number...resending ACK attempt # " + device.mACKRetryCount);
						resendMessage(device, device.mLastSentACK);
					} else {
						Log.e(TAG, "Duplicate ascension number...ACK already resent the maximum number of times (" + ACK_RETRY_ATTEMPTS + "). Ignoring message...");
					}				
				} else {
					if (D) Log.d(TAG, "Updating device info...");
					// update the device info
					device.setDeviceType(header.deviceType);
					device.mMessageType = header.messageType;
					device.mRestrictedErrorSection = restrictedErrorSection;
					device.setDeviceOperatingMode(deviceOperatingMode);
					device.mSensorOperatingMode = sensorOperatingMode;
					device.mRestrictedStatusBits = restrictedStatusBits;
					device.mSensorErrorBits = sensorErrorBits;
					device.setMechanicalSealId(mechanicalSealId);
					device.setConveyanceId(conveyanceId);
					device.setManifest(manifest);
					device.setAlarmStatus(alarmStatus);
					device.setDoorStatus(doorStatus);
					device.mReceiveAscensionRestricted = header.messageAscensionNumber;
				}
			}
		} else {
			if (D) Log.d(TAG, "Adding new device to list...");
			device = new DeviceInfo(header.deviceType, header.messageType, header.deviceUID, 
									restrictedErrorSection, deviceOperatingMode, sensorOperatingMode, 
									restrictedStatusBits, sensorErrorBits, mechanicalSealId, 
									conveyanceId, manifest, alarmStatus, doorStatus, 
									header.messageAscensionNumber);
			mAndroNadService.addDeviceInfo(device);
		}
		
		// send ACK if message was valid and unsolicited
		if (messageValid && (restrictedErrorSection & RESTRICTED_ERROR_UNSOLICITED) != 0) {
			// send ACK
			if (D) Log.i(TAG, "Sending ACK...");
			synchronized (device) {
				sendACK(device.getUID(), (byte)(device.mReceiveAscensionRestricted & 0xff));	
			}
		} // if message was valid and solicited
		else if (messageValid && (restrictedErrorSection & RESTRICTED_ERROR_UNSOLICITED) == 0) {
			// check to see if it is an ACK/Solicited status to a command
			// and ascension number matches
			synchronized (device) {
				if (message[RSM_ACK_ASCENSION_NUMBER] == (Utils.bytesToInt(device.mLastSentCommand, MH_MESSAGE_ASCENSION_NUMBER) & 0xff)) {
					device.cancelRetryTimer(); // cancel timer
					device.mCommandRunning = false;
					device.mSendAscensionRestricted++;
					if(D) Log.d(TAG, "Solicited status received from " + device.getUID() + 
							" in response to RCMD 0x" + String.format("%02X", device.mLastSentCommandOpcode));
				} else {
					Log.e(TAG, "Solicitied status received from " + device.getUID() + ", but with wrong ascension.");
				}
			}
		}
	}
	
	/**
	 * Parse an unrestricted status message.
	 * 
	 * @param message
	 * @param length
	 * @param header
	 */
	private void parseUnrestrictedStatus(byte[] message, int length, MessageHeader header) {
		if (D) Log.d(TAG, "Unrestricted status message received.");
		
		// TODO	do something with the message body.
		byte deviceSection = message[USM_DEVICE_SECTION];
		
		boolean unsolicited = false;
		DeviceInfo device = mAndroNadService.getDeviceInfo(header.deviceUID);
		if (device != null) {
			synchronized (device) {
				unsolicited = (device.mCommandRunning && (deviceSection & DEVICE_SECTION_UNSOLICITED) == 0); 
			}
		} else {
			if (D) Log.i(TAG, "Unrestricted status message received from unknown device. Ignoring...");
			return;
		}
		
		// if message was unsolicited and expected
		// TODO no ascension check right now
		synchronized (device) {
			if (!unsolicited && device.mCommandRunning && device.mLastSentCommandType == MESSAGE_UNRESTRICTED_COMMAND && device.mLastSentCommandType == UCMD_OPCODE_USTATUS) {
				if (D) Log.i(TAG, "Unrestricted status message received in response to USTATUS command.");
				
				// update device info
				device.setDeviceType(header.deviceType);
				device.mMessageType = header.messageType;
				
				device.mCommandRunning = false;
				device.cancelRetryTimer();
			}
		}
	}
	
	/**
	 * Parse an event log message.
	 * 
	 * @param message
	 * @param length
	 * @param header
	 */
	private void parseEventLogRecord(byte[] message, int length, MessageHeader header) {
		if (D) Log.d(TAG, "Event log message received.");

		if (useEncryption()) {
			try {
				decrypt(message);
			} catch (Exception e) {
				Log.e(TAG, "Log message decryption failed. Discarding...");
				return;
			}
		}
		
		byte	logCommandAck			= message[EL_LOG_REQUEST_ACK_NUMBER];
		byte	eventType				= message[EL_EVENT_TYPE];
		byte	deviceOperatingMode		= message[EL_DEVICE_OPERATING_MODE];
		byte	restrictedStatusBits	= message[EL_RESTRICTED_STATUS_BITS];
		byte	restrictedErrorBits		= message[EL_RESTRICTED_ERROR_BITS];
		byte[]  eventTime				= new byte[UTC_LENGTH];
		System.arraycopy(message, EL_EVENT_TIME, eventTime, 0, UTC_LENGTH);
		String	conveyanceId;			
		try {
			// + 1 and - 1 because we are ignoring the first byte of conveyance id since there is only one container type right now.
			conveyanceId				= new String(message, EL_CONVEYANCE_ID + 1, CONVEYANCE_ID_LENGTH - 1, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error parsing restricted status message. Discarding...", e);
			return;
		}
		byte	alarmStatus				= message[EL_ALARM_STATUS];
		byte	doorStatus				= message[EL_DOOR_STATUS];
		byte	commandMessageType		= message[EL_COMMAND_MESSAGE_TYPE];
		byte	commandOpcode			= message[EL_COMMAND_OPCODE];
		
		boolean messageValid = true;
		DeviceInfo device = mAndroNadService.getDeviceInfo(header.deviceUID);
		if (device != null) {
			synchronized (device) {
				// check ascension number
				if (header.messageAscensionNumber < device.mReceiveAscensionRestricted) {
					// drop message
					if (D) Log.i(TAG, "Invalid ascension number...dropping message");
					messageValid = false;
				} else if (header.messageAscensionNumber == device.mReceiveAscensionRestricted) {
					messageValid = false;
					if (device.mACKRetryCount < ACK_RETRY_ATTEMPTS) {
						// resend ack
						device.mACKRetryCount++;
						if (D) Log.i(TAG, "Duplicate ascension number...resending ACK attempt # " + device.mACKRetryCount);
						resendMessage(device, device.mLastSentACK);
					} else {
						Log.e(TAG, "Duplicate ascension number...ACK already resent the maximum number of times (" + ACK_RETRY_ATTEMPTS + "). Ignoring message...");
					}				
				} else if (logCommandAck != device.mEventLogCommandAscension) {
					messageValid = false;
					Log.e(TAG, "Event log message ack (" + logCommandAck + ") does not match log command ascension (" + device.mEventLogCommandAscension + "). Discarding...");
				}
			}
		} else {
			Log.e(TAG, "Event record from unknown device. Discarding...");
		}
		
		synchronized (device) {
			// message valid and expected
			if (messageValid && device.mWaitingForLog) {
				
				String logEntry = "";
				
				// This is the last log message
				if (eventType == EVENT_END_OF_RECORDS) {
					device.mWaitingForLog = false;
					logEntry += "End of records.";
				} else {
					// parse message normally
					// parse date
					
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.set(eventTime[UTC_YEARS_SINCE_2000] + 2000, 
								eventTime[UTC_MONTH], eventTime[UTC_DAY_OF_MONTH], 
								eventTime[UTC_HOURS_SINCE_MIDNIGHT], 
								eventTime[UTC_MINUTES_AFTER_HOUR], 
								eventTime[UTC_SECONDS_AFTER_MINUTE]);
					
					logEntry += String.format("%1$tF %1$tT UTC", calendar) + " -";
					logEntry += " Event type: " + String.format("0x%02X", eventType);
					logEntry += ", Armed: " + (deviceOperatingMode == DeviceInfo.OP_MODE_ARMED ? "YES" : "NO");
					logEntry += ", Restricted status bits: " + String.format("0x%02X", restrictedStatusBits);
					logEntry += ", Restricted error bits: " + String.format("0x%02X", restrictedErrorBits);
					logEntry += ", Conveyance: " + conveyanceId.trim();
					logEntry += ", Alarm: " + (alarmStatus == DeviceInfo.ALARM_YES ? "YES" : "NO");
					logEntry += ", Door: " + (doorStatus == DeviceInfo.DOOR_OPEN ? "OPEN" : "CLOSED");
					
					if (eventType == EVENT_STATUS_CHANGE_COMMAND) {
						logEntry += ", Command type: " + String.format("0x%02X", commandMessageType);
						logEntry += ", Opcode: " + String.format("0x%02X", commandOpcode);
					}
				}
				
				if (D) Log.i(TAG, logEntry);
				device.addLogEntry(logEntry);
				
				// update device info
				device.setDeviceType(header.deviceType);
				device.mMessageType = header.messageType;
				device.mReceiveAscensionRestricted = header.messageAscensionNumber;
				
				// send ACK
				sendACK(device.getUID(), (byte) (device.mReceiveAscensionRestricted & 0xff));
			} else {
				Log.e(TAG, "Received unexpected log record. Discarding...");
			}
		}
	}
	
	private void resendMessage(DeviceInfo destinationDevice, byte[] message) {
		// send the message
		if (message != null)
			mAndroNadService.sendMessage(destinationDevice.getUID(), message);
	}

	private void sendACK(String destinationUid, byte ascension) {
		byte[] payload = new byte[2];
		payload[0] = RCMD_OPCODE_ACK;
		payload[1] = ascension;
		sendRestrictedCommand(destinationUid, payload);
	}
	
	/**
	 * Send a NOP (no operation) command.
	 * 
	 * @param destinationUid
	 */
	public void sendNop(String destinationUid) {
		byte[] payload = new byte[1];
		payload[0] = RCMD_OPCODE_NOP;
		sendRestrictedCommand(destinationUid, payload);
	}
	
	/**
	 * Set the master alarm.
	 * 
	 * @param destinationUid
	 * @param alarm				true sends SMAT command, false sends SMAF command
	 */
	public void sendSetMasterAlarm(String destinationUid, boolean alarm) {
		byte[] payload = new byte[1];
		payload[0] = (alarm ? RCMD_OPCODE_SMAT : RCMD_OPCODE_SMAF);
		sendRestrictedCommand(destinationUid, payload);
	}  
	
	/**
	 * Send the disarm from DCP command.
	 * 
	 * @param desinationUid
	 */
	public void sendDisarm(String destinationUid) {
		byte[] payload = new byte[1];
		payload[0] = RCMD_OPCODE_DADC;
		sendRestrictedCommand(destinationUid, payload);
	}
	
	public void sendSetTime(String destinationUid) {
		byte[] payload = new byte[1 + RCMD_TIME_LENGTH];
		payload[0] = RCMD_OPCODE_ST;
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Date format: ICD table 9-1
		payload[1 + UTC_MONTH] = (byte) (calendar.get(Calendar.MONTH) + 1);
		payload[1 + UTC_DAY_OF_MONTH] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
		payload[1 + UTC_YEARS_SINCE_2000] = (byte) (calendar.get(Calendar.YEAR) - 2000);
		payload[1 + UTC_DAY_OF_WEEK] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
		payload[1 + UTC_HOURS_SINCE_MIDNIGHT] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		payload[1 + UTC_MINUTES_AFTER_HOUR] = (byte) calendar.get(Calendar.MINUTE);
		payload[1 + UTC_SECONDS_AFTER_MINUTE] = (byte) calendar.get(Calendar.SECOND);
		payload[1 + UTC_FRACTIONAL_SECONDS] = 0x00; // unused
		sendRestrictedCommand(destinationUid, payload);
	}
	
	/**
	 * Send trip info with either ARMT or CTI.
	 * @param destinationUid
	 * @param opcode
	 * @param mechanicalSealId
	 * @param conveyanceId
	 * @param manifest
	 */
	public void sendTripInformation(String destinationUid, byte opcode, String mechanicalSealId, 
			String conveyanceId, String manifest) {
		// add leading spaces to mechanical seal id up to the max length
		while (mechanicalSealId.length() < MECHANICAL_SEAL_ID_LENGTH)
			mechanicalSealId = " " + mechanicalSealId;
		
		byte[] payload = new byte[1 + MECHANICAL_SEAL_ID_LENGTH + CONVEYANCE_ID_LENGTH + 
		                          MANIFEST_LENGTH + 
		                          (opcode == RCMD_OPCODE_ARMT ? 1 : 0)]; // add 1 if armt to enable sensors
		int index = 0; // index of where we are in the payload array
		
		payload[index] = opcode;
		index++;
		
		byte[] conveyanceBytes;
		byte[] mechSealBytes;
		byte[] manifestBytes;
		
		try {
			conveyanceBytes  = conveyanceId.getBytes("US-ASCII");
			mechSealBytes	 = mechanicalSealId.getBytes("US-ASCII");
			manifestBytes	 = manifest.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Error encoding String as bytes. Sending command failed.", e);
			return;
		}
		
		// Copy conveyance
		payload[index] = CONVEYANCE_ID_FORMAT;
		index++;
		System.arraycopy(conveyanceBytes, 0, payload, index, conveyanceBytes.length);
		index += CONVEYANCE_ID_LENGTH - 1;
		
		// Copy manifest
		System.arraycopy(manifestBytes, 0, payload, index, manifestBytes.length);
		index += MANIFEST_LENGTH;
		
		// Copy mech seal
		System.arraycopy(mechSealBytes, 0, payload, index, mechSealBytes.length);
		index += MECHANICAL_SEAL_ID_LENGTH;
		
		if (opcode == RCMD_OPCODE_ARMT) {
			// turn on sensors
			if (D) Log.i(TAG, "ARMT with all sensors on.");
			payload[index] = (byte) 0xff;
			index++;
		}
		
		// send the command
		sendRestrictedCommand(destinationUid, payload);
	}
	
	/**
	 * Set the in-trip state to a state defined in ICD table 9-9.
	 * @param destinationUid
	 * @param state
	 */
	public void sendSetIntripState(String destinationUid, byte state) {
		byte[] payload = new byte[2];
		payload[0] = RCMD_OPCODE_SIS;
		payload[1] = state;
		sendRestrictedCommand(destinationUid, payload);
	}
	
	/**
	 * Send the log command. Payload is only the opcode.
	 * @param destinationUid
	 * @param opcode
	 */
	public void sendLogCommand(String destinationUid, byte opcode) {
		byte[] payload = new byte[1];
		payload[0] = opcode;
		sendRestrictedCommand(destinationUid, payload);
	}
	
	private void sendRestrictedCommand(String destinationUid, byte[] payload) {
		DeviceInfo device = mAndroNadService.getDeviceInfo(destinationUid);
		if (device == null) {
			Log.e(TAG, "Failure sending unrestricted status command to " + destinationUid);
			return;
		}
		
		if (D) Log.d(TAG, "Send restricted command with payload size=" + payload.length);
		
		byte[] message = new byte[MH_HEADER_LENGTH + payload.length + MIC_LENGTH];
		MessageHeader header = new MessageHeader();
		synchronized (device) {
			header.deviceType = DEVICE_TYPE;
			header.icdRevNumber = ICD_REV_NUMBER;
			header.messageLength = (byte) (payload.length + MIC_LENGTH);
			header.messageType = MESSAGE_RESTRICTED_COMMAND;
			header.deviceUID = mDeviceUID;
			header.messageAscensionNumber = device.mSendAscensionRestricted;
			header.toBytes(message, 0);
			
			// copy payload into message
			System.arraycopy(payload, 0, message, RCMD_RESTRICTED_SECTION, payload.length);
			
			// if ACK
			if (message[RCMD_OPCODE] == RCMD_OPCODE_ACK) {
				device.mACKRetryCount = 0;
				device.mLastSentACK = message;
				device.mSendAscensionRestricted++;
			} // other command 
			else {
				// if command/log already pending, don't send another
				if (device.mCommandRunning || device.mWaitingForLog) {
					String msg = mAndroNadService.getResources().getString(R.string.toast_pending_command)
						.replace("<device>", device.getUID());
					if (D) Log.d(TAG, msg);
					mAndroNadService.toastMessage(msg);
					return;
				}
	
				// save changes to device info
				device.mLastSentCommand 	= message;
				device.mCommandRetryCount	= 0;
				device.mCommandRunning 		= true;	
				
				// last sent command type and opcode
				device.mLastSentCommandType = header.messageType;
				device.mLastSentCommandOpcode = message[RCMD_OPCODE];
				
				addRetryTimer(device, true);
				
				// save the ascension number for the SL/SLU command
				if (device.mLastSentCommandOpcode == RCMD_OPCODE_SL || 
						device.mLastSentCommandOpcode == RCMD_OPCODE_SLU) {
					device.mEventLogCommandAscension = (byte) (header.messageAscensionNumber & 0xff);
					device.mWaitingForLog = true;
				} 
			}
		}
		
		// encrypt message before sending
		if (useEncryption()) {
			try {
				encrypt(message);
			} catch (Exception e) {
				Log.e(TAG, "Command message encryption failed. Discarding...", e);
				if (message[RCMD_OPCODE] == RCMD_OPCODE_ACK) 
					mAndroNadService.toastMessage(mAndroNadService.getResources().getString(R.string.toast_ack_enc_error));
				else
					mAndroNadService.toastMessage(mAndroNadService.getResources().getString(R.string.toast_command_enc_error));
				return;
			}
		}
		
		if (D) Log.d(TAG, "Sending restricted command to " + device.getUID());
		if (D) Log.d(TAG, header.toString());
		if (D) Log.d(TAG, Utils.bytesToHexString(message, 0, message.length));
		
		// send the message
		mAndroNadService.sendMessage(device.getUID(), message);
	}
	
	public void sendUnrestrictedStatusCommand(String destinationUid) {
		byte[] payload = new byte[1];
		payload[0] = UCMD_OPCODE_USTATUS;
		sendUnrestrictedCommand(destinationUid, payload);
	}
	
	private void sendUnrestrictedCommand(String destinationUid, byte[] payload) {
		DeviceInfo device = mAndroNadService.getDeviceInfo(destinationUid);
		if (device == null) {
			Log.e(TAG, "Failure sending unrestricted status command to " + destinationUid);
			return;
		}
		
		byte[] message = new byte[MH_HEADER_LENGTH + payload.length + (USE_xCSD_COMPATIBILITY ? 0 :UCMD_CHECKSUM_LENGTH)];
		MessageHeader header = new MessageHeader();
		synchronized (device) {
			header.deviceType = DEVICE_TYPE;
			header.icdRevNumber = ICD_REV_NUMBER;
			header.messageLength = (byte) (payload.length + (USE_xCSD_COMPATIBILITY ? 0 :UCMD_CHECKSUM_LENGTH)); // ICD 7.1 does not have the checksum
			header.messageType = MESSAGE_UNRESTRICTED_COMMAND;
			header.deviceUID = mDeviceUID;
			header.messageAscensionNumber = 0; // TODO zero is ascension number for now, since there is no ascension check ?
			header.toBytes(message, 0);
			
			// copy payload into message
			System.arraycopy(payload, 0, message, UCMD_UNRESTRICTED_SECTION, payload.length);
			
			// last sent command type and opcode
			device.mLastSentCommandType = header.messageType;
			device.mLastSentCommandOpcode = message[UCMD_OPCODE];
			
			if (!USE_xCSD_COMPATIBILITY && USE_UNRESTRICTED_CHECKSUM) {
				// TODO calculate 1 byte checksum of header and payload
			}
			
			// if command/log already pending, don't send another
			if (device.mCommandRunning || device.mWaitingForLog) {
				String msg = mAndroNadService.getResources().getString(R.string.toast_pending_command)
					.replace("<device>", device.getUID());
				if (D) Log.d(TAG, msg);
				mAndroNadService.toastMessage(msg);
				return;
			}
			
			// save changes to device info
			device.mLastSentCommand 		= message;
			device.mCommandRetryCount	= 0;
			device.mCommandRunning 		= true;		
			addRetryTimer(device, false);
		}
		if (D) Log.d(TAG, "Sending unrestricted command to " + device.getUID());
		if (D) Log.d(TAG, header.toString());
		if (D) Log.d(TAG, Utils.bytesToHexString(message, 0, message.length));
		
		// send the message
		mAndroNadService.sendMessage(device.getUID(), message);
	}
	
	/**
	 * Add a retry timer to the device.
	 * 
	 * @param device
	 */
	private void addRetryTimer(final DeviceInfo device, final boolean restricted) {
		if (device == null)
			return;
		
		// need to create count
		mAndroNadService.runOnMainThread(new Runnable() {
			@Override
			public void run() {
				device.setRetryTimer(new CountDownTimer(COMMAND_TIMEOUT, COMMAND_TIMEOUT * 2) {
					// Won't get called set we set interval to the timeout * 2
					@Override
					public void onTick(long millisUntilFinished) {}
					
					@Override
					public void onFinish() {
						if (device.mCommandRunning && device.mCommandRetryCount < COMMAND_RETRY_ATTEMPTS) {
							resendMessage(device, device.mLastSentCommand);
							device.mCommandRetryCount++;
							this.start(); // restart timer
							
							if (D) Log.d(TAG, "Timeout: command resend # " + device.mCommandRetryCount + " to " + device.getUID());
							mAndroNadService.toastMessage("Timeout: command resend # " + device.mCommandRetryCount + " to " + device.getUID());
						} else {
							// abort command, retries exceeded
							device.mCommandRunning = false;
							device.mWaitingForLog  = false;
							device.cancelRetryTimer();
							// throw away bad ascension number
							if (restricted)
								device.mSendAscensionRestricted++;
							
							Log.e(TAG, "Command failed: exceeded " + COMMAND_RETRY_ATTEMPTS + " retry attempts to " + device.getUID());
							mAndroNadService.toastMessage("Command failed: exceeded " + COMMAND_RETRY_ATTEMPTS + " retry attempts to " + device.getUID());
						}
					}
				});
			}
		});
	}
	
	/**
	 * Wrapper class to pass header data around easily. Also provides convenience of
	 * writing a message header to a byte array.
	 * 
	 * @author Cory Sohrakoff
	 *
	 */
	private class MessageHeader {
		public byte deviceType;
		public byte messageType;
		public byte messageLength;
		public String deviceUID;
		public byte icdRevNumber;
		public int messageAscensionNumber; // ascension is 4 bytes so an int works
		
		@Override
		public String toString() {
			return  "DeviceType=0x" + String.format("%02X", deviceType) + 
					",MessageType=0x" + String.format("%02X", messageType) + 
					",MessageLength=" + messageLength + 
					",DeviceUID=" + deviceUID + 
					",ICDRevNumber=" + icdRevNumber + 
					",MessageAscensionNumber=" + messageAscensionNumber;
		}
		
		/**
		 * Dump the header to its byte representation.
		 * 
		 * @param bytes
		 * @param offset
		 */
		public void toBytes(byte[] bytes, int offset) {
			if (bytes == null || offset + MH_HEADER_LENGTH > bytes.length) {
				Log.e(TAG, "MessageHeader.toBytes() error");
				return;
			}
			bytes[MH_DEVICE_TYPE + offset] = deviceType;
			bytes[MH_MESSAGE_TYPE + offset] = messageType;
			bytes[MH_MESSAGE_LENGTH + offset] = messageLength;
			Utils.hexStringToBytes(deviceUID, bytes, MH_DEVICE_UID);
			bytes[MH_ICD_REV_NUMBER] = icdRevNumber;
			Utils.intToBytes(messageAscensionNumber, bytes, MH_MESSAGE_ASCENSION_NUMBER);
		}
	}
}
