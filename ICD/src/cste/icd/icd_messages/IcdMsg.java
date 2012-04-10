package cste.icd.icd_messages;

import java.nio.ByteBuffer;

import cste.icd.components.ComModule;
import cste.icd.general.IcdPayload;
import cste.icd.general.KeyProvider;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import cste.icd.types.IcdHeader;
import cste.icd.types.MsgType;
import cste.icd.types.UnrestrictedCmdType;
import static cste.icd.general.Utility.*;
import static cste.icd.general.Constants.*;

/***
 * 
 * @author Sergio Enriquez
 *
 */
public class IcdMsg {
	static String TAG = "ICD Msg";
	
	public enum MsgStatus{
		BAD_CHEKSUM,
		WRONG_SIZE,
		ENCRYPTION_ERROR,
		EMPTY_MSG,
		OTHER,
		BAD_CONFIG,
		MISSING_PARAMETERS,
		UNSUPORTED_MSG,
		OK
	}
	
	private static DeviceType ThisDevType = null;
	private static DeviceUID ThisUID = null;
	private static KeyProvider KeyProvider = null;
	private static boolean EncryptionEnabled = false;
	
	final public IcdHeader header;
	final public IcdPayload payload;
	final public MsgStatus msgStatus;
	final public DeviceUID destUID;

	/***
	 * This function should be called one before generating any ICD packets.
	 * @param type
	 * @param uid
	 * @param rev
	 */
	public static void configure(boolean encryptionEnabled, DeviceType type, DeviceUID uid, KeyProvider keyProv){
		EncryptionEnabled = encryptionEnabled;
		ThisDevType = type;
		ThisUID = uid;
		KeyProvider = keyProv;
	}

	/***
	 * Creates an ICD message object from an byte array input
	 * If there is an error it will return an empty message object with the error code set
	 * @param payload
	 * @return
	 */
	public static IcdMsg fromBytes(byte[] payload){
		if( payload == null || payload.length == 0)
			return new IcdMsg(MsgStatus.WRONG_SIZE);
		else
			return parseBytes(payload);
	}
	
	/***
	 * Creates a new ICD message byte array based on the destination, msg type, and parameters
	 * Encryption will be performed if required. It will return null if there is an error.
	 * @param dest
	 * @param msgType
	 * @param payload
	 * @return byte array if successful, null if error
	 */
	public static IcdMsg buildIcdMsg(ComModule destination, MsgType msgType, Object ... params){
		IcdPayload payload = null;
		DeviceType devType = ThisDevType;

		switch(msgType){
		case DEV_CMD_RESTRICTED:
			devType = DeviceType.DCP;
			if( params.length >= 1){
				if ( destination.devType == DeviceType.ECOC || destination.devType == DeviceType.ECM0 )
					payload = RestrictedCmdECM.create(params);
				else if ( destination.devType == DeviceType.ACSD || destination.devType == DeviceType.CSD)
					payload = RestrictedCmdECM.create(params); //TODO replace with appropiate type
				else
					Log(TAG, "Config error");
			}
			break;
		case DEV_CMD_UNRESTRICTED:
			devType = DeviceType.DCP;
			if( params.length == 1)
				payload = new UnrestrictedCmd((UnrestrictedCmdType)params[0]);
			else
				Log(TAG, "Config error");
			break;
		default:
			Log(TAG, "Trying to build unsupported icd msg type");
		}
		
		if( payload == null)
			return null;
		
		IcdHeader header = new IcdHeader(
				devType,
				msgType,
				payload.getSize(),
				ThisUID,
				ICD_REV_NUMBER,
				destination.txAscension);
		
		return new IcdMsg(destination.devUID,header,payload);
	}
	
	public byte[] getBytes(){
		byte []headerBytes = header.getBytes();
		byte []payloadBytes = payload.getBytes();
		if (EncryptionEnabled && header.msgType.isEncypted()){
			byte[] key = KeyProvider.getEncryptionKey(destUID);
			payloadBytes = encrypt(payloadBytes,key, header.getNonce());
			if ( payloadBytes == null)
				return null;
		} 
		
		ByteBuffer buffer = ByteBuffer.allocate(IcdHeader.SECTION_SIZE + payload.getSize());
		buffer.put(headerBytes);
		buffer.put(payloadBytes);
		
		if( header.msgType == MsgType.DEV_CMD_UNRESTRICTED){
			byte chkSum = 0;
			for(byte b : headerBytes)
				chkSum += b;
			for(byte b : payloadBytes)
				chkSum += b;
			buffer.put(chkSum);
		}
		
		return buffer.array();
	}

	private static IcdMsg parseBytes(byte[] data){
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		//special case for NULL MSG
		if( buffer.capacity() == NullMsg.SECTION_SIZE){	
			NullMsg msg = new NullMsg(buffer);
			IcdHeader hdr = new IcdHeader(
					DeviceType.fromValue(msg.devType),
					MsgType.NULL_MSG,
					(byte)NullMsg.SECTION_SIZE,
					msg.devUID,
					(byte)2,
					0);
			
			return new IcdMsg(ThisUID, 
					hdr, // place holder
					msg);
		}
		
		IcdHeader headerData = IcdHeader.fromBuffer(buffer);
		if ( headerData == null){
			return new IcdMsg(MsgStatus.WRONG_SIZE);
		}
		
		if (EncryptionEnabled && headerData.msgType.isEncypted()){
			byte []encryptedPayload = new byte [headerData.payloadSize];
			buffer.get(encryptedPayload);
			
			byte[] key = KeyProvider.getEncryptionKey(headerData.devUID);
			byte[] decryptedPayload = null;
			if (key != null )
				decryptedPayload = decrypt(encryptedPayload,key, headerData.getNonce());
			
			if ( decryptedPayload == null)
				return new IcdMsg(MsgStatus.ENCRYPTION_ERROR);
			buffer = ByteBuffer.wrap(decryptedPayload);
		}
		
		IcdPayload msgContent=null;
		switch(headerData.msgType){
		case UNRESTRICTED_STATUS_MSG:
			msgContent = new UnrestrictedStatusECM(buffer);
			break;
		case RESTRICTED_STATUS_MSG:
			if( headerData.devType == DeviceType.ECOC || headerData.devType == DeviceType.ECM0)
				msgContent = new RestrictedStatusECM(buffer);
			else if (headerData.devType == DeviceType.CSD || headerData.devType == DeviceType.ACSD)
				msgContent = new RestrictedStatusCSD(buffer);
			break;
		case DEVICE_EVENT_LOG:
			if( headerData.devType == DeviceType.ECOC || headerData.devType == DeviceType.ECM0)
				msgContent = new EventLogECM(buffer);
			else if (headerData.devType == DeviceType.CSD || headerData.devType == DeviceType.ACSD)	
				msgContent = new EventLogCSD(buffer);
			break;
		case DEV_CMD_RESTRICTED:
			if( headerData.devType == DeviceType.ECOC || headerData.devType == DeviceType.ECM0)
				msgContent = RestrictedCmdECM.fromBuffer(buffer);
			else if (headerData.devType == DeviceType.CSD || headerData.devType == DeviceType.ACSD)	
				msgContent = null;
			break;
		case NULL_MSG:
			msgContent = new NullMsg(buffer);
			break;
		case NADA_MSG:
			msgContent = null;
			return new IcdMsg(MsgStatus.UNSUPORTED_MSG);
		default:
			MsgType type = headerData.msgType;
			Log(TAG, "Received unknown msg type " + type.toString());
			return new IcdMsg(MsgStatus.BAD_CONFIG);
		}

		if( msgContent == null)
			return new IcdMsg(MsgStatus.WRONG_SIZE);

		return new IcdMsg(ThisUID, headerData,msgContent);
	}
	

	public String toString(){
		if( header == null || payload == null)
			return "NO DATA";
		else
			return "Dest:" + destUID.toString() + " " + header.toString() + " " + payload.toString();
	}

	private IcdMsg(MsgStatus error){
		this.destUID = null;
		this.header = null;
		this.payload = null;
		this.msgStatus = error;
	}
	
	private IcdMsg(
			DeviceUID destUID,
			IcdHeader headerData,
			IcdPayload payload){
		this.destUID = destUID;
		this.header = headerData;
		this.payload = payload;
		this.msgStatus = MsgStatus.OK;
	}
	
	@SuppressWarnings("unused")
	private static boolean checksumOK(ByteBuffer buffer){
		byte sum = 0;
		int i;
		for(i=0;i<buffer.capacity()-1;i++)
			sum += buffer.get(i);
		byte checksum = buffer.get(i);
		buffer.rewind();
		if ( sum == checksum)
			return true;
		else
			return false;
	}
	
}	
