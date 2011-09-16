package cste.icd;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ICD {	
	// discard ICD messages if the Rev Number is not the same as this ICD implementation
	private static final boolean DISCARD_WRONG_VERSION = true;
	
	// use encryption where required
	private boolean mUseEncryption = true;
	
	public synchronized boolean useEncryption() {
		return mUseEncryption;
	}

	// use unrestricted checksum
	private static final boolean USE_UNRESTRICTED_CHECKSUM 	= false;
	
	// Message encryption key length
	public static final int ENCRYPTION_KEY_LENGTH			= 16;
	public  static final int ENCRYPTION_KEY_LENGTH_IN_HEX	= ENCRYPTION_KEY_LENGTH * 2;
	
	/**
	 * Default encryption key for xCSD in lab.
	 */
	private String mEncryptionKey = "da486be78ab8830e10d657f66ea7d8db";

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

	public byte[] encryptAES(byte[] message, byte[] encryptionKey)throws Exception{
		Key key = new SecretKeySpec(encryptionKey, "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] encValue = c.doFinal(message);
		return encValue;
	}
	
	public static byte[] decryptAES(byte[] message, byte[] encryptionKey) throws Exception {
	        Key key = new SecretKeySpec(encryptionKey, "AES");
	        Cipher c = Cipher.getInstance("AES");
	        c.init(Cipher.DECRYPT_MODE, key);
	        byte[] decValue = c.doFinal(message);
	        return decValue;
    }

}
