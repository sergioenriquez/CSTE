package cste.messages;

import java.nio.ByteBuffer;

import cste.components.ComModule;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;
import cste.icd.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import static cste.icd.Utility.*;

/***
 * 
 * @author Sergio Enriquez
 *
 */
public class IcdMsg {
	private static String TAG = "ICD Msg class";
	public enum MsgStatus{
		BAD_CHEKSUM,
		WRONG_SIZE,
		ENCRYPTION_ERROR,
		EMPTY_MSG,
		OTHER,
		BAD_CONFIG,
		MISSING_PARAMETERS,
		OK
	}
	
	private static DeviceType ThisDevType = null;
	private static DeviceUID ThisUID = null;
	private static byte IcdRev = 0;
	private static KeyProvider KeyProvider = null;
	private static boolean EncryptionEnabled = false;
	
	final private IcdHeader headerData;
	final private IcdPayload payload;
	final private MsgStatus msgStatus;
	

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
		IcdRev = 0x02;
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
	public static byte[] buildIcdMsg(ComModule destination, MsgType msgType, Object ... params){
		IcdPayload payload = null;
		DeviceType devType = ThisDevType;

		switch(msgType)
		{
		case DEV_CMD_RESTRICTED:
			devType = DeviceType.DCP;
			if( params.length >= 1)
			{
				if ( destination.devType() == DeviceType.ECOC || destination.devType() == DeviceType.ECM0 )
					payload = RestrictedEcocCmd.create(params);
				else if ( destination.devType() == DeviceType.ACSD || destination.devType() == DeviceType.CSD)
					payload = RestrictedEcocCmd.create(params); //TODO replace with appropiate type
				else
					Log(TAG, "Config error");
			}
			break;
		case DEV_CMD_UNRESTRICTED:
			devType = DeviceType.DCP;
			if( params.length == 1)
				payload = new UnrestrictedCommand((UnrestrictedCmdType)params[0]);
			else
				Log(TAG, "Config error");
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
				IcdRev,
				destination.getTxAsc());
		
		byte []headerBytes = header.getBytes();
		byte []payloadBytes = payload.getBytes();
		if (EncryptionEnabled && msgType.isEncypted()){
			byte[] key = KeyProvider.getEncryptionKey(destination.UID());
			payloadBytes = encrypt(payloadBytes,key, header.getNonce());
			if ( payloadBytes == null)
				return null;
		} 
		
		ByteBuffer buffer = ByteBuffer.allocate(header.getHdrSize() + payload.getSize());
		buffer.put(headerBytes);
		buffer.put(payloadBytes);
		
		if( msgType == MsgType.DEV_CMD_UNRESTRICTED)
		{
			byte chkSum = 0;
			for(byte b : headerBytes)
				chkSum += b;
			for(byte b : payloadBytes)
				chkSum += b;
			buffer.put(chkSum);
		}
		
		destination.incTxAsc();
		
		return buffer.array();
	}

	public MsgType msgType(){
		return headerData.getMsgType();
	}

	public IcdHeader header(){
		return headerData;
	}
	
	public Object payload(){
		return payload;
	}
	
	public MsgStatus getStatus(){
		return msgStatus;
	}
	
	public String toString(){
		if( headerData == null || payload == null)
			return "NO DATA";
		else
			return headerData.toString() + " " + payload.toString();
	}

	private IcdMsg(MsgStatus error){
		this.headerData = null;
		this.payload = null;
		this.msgStatus = error;
	}
	
	private IcdMsg(
			DeviceUID destinationUID,
			IcdHeader headerData,
			IcdPayload payload){
		this.headerData = headerData;
		this.payload = payload;
		this.msgStatus = MsgStatus.OK;
	}
	
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
	
	private static IcdMsg parseBytes(byte[] data){
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		if ( !checksumOK(buffer) )
			return new IcdMsg(MsgStatus.BAD_CHEKSUM);
		
		IcdHeader headerData = IcdHeader.fromBuffer(buffer);
		if ( headerData == null){
			return new IcdMsg(MsgStatus.WRONG_SIZE);
		}
		
		if (EncryptionEnabled && headerData.getMsgType().isEncypted()){
			byte []encryptedPayload = new byte [headerData.getPayloadSize()];
			buffer.get(encryptedPayload);
			
			byte[] key = KeyProvider.getEncryptionKey(headerData.devUID);
			byte[] decryptedPayload = decrypt(encryptedPayload,key, headerData.getNonce());
			if ( decryptedPayload == null)
				return new IcdMsg(MsgStatus.ENCRYPTION_ERROR);
			buffer = ByteBuffer.wrap(decryptedPayload);
		}
		
		IcdPayload msgContent=null;
		switch(headerData.getMsgType()){
		case UNRESTRICTED_STATUS_MSG:
			msgContent = UnrestrictedStatusMsg.fromBuffer(buffer);
			break;
		case RESTRICTED_STATUS_MSG:
			if( headerData.devType == DeviceType.ECOC || 
				headerData.devType == DeviceType.ECM0)
				msgContent = ECoCRestrictedStatus.fromBuffer(buffer);
			else if (headerData.devType == DeviceType.CSD || 
					headerData.devType == DeviceType.ACSD)
				msgContent = ECoCRestrictedStatus.fromBuffer(buffer);
			break;
		case DEVICE_EVENT_LOG:
			msgContent = EventLogMsg.fromBuffer(headerData.getDevType(),buffer);
			break;
		case NADA_MSG:
			break;
		default:
			MsgType type = headerData.getMsgType();
			Log(TAG, "Received unknown msg type " + type.toString());
			return new IcdMsg(MsgStatus.BAD_CONFIG);
		}

		if( msgContent == null)
			return new IcdMsg(MsgStatus.WRONG_SIZE);

		return new IcdMsg(ThisUID, headerData,msgContent);
	}
}	
