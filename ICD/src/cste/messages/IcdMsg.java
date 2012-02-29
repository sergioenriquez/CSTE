package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;
import cste.icd.SecurityDevice;
import cste.icd.UnrestrictedCmdType;
import cste.interfaces.KeyProvider;
import static cste.icd.Utility.*;

public class IcdMsg {
	public enum MsgStatus{
		BAD_CHEKSUM,
		WRONG_SIZE,
		EMPTY_MSG,
		OTHER,
		BAD_CONFIG,
		MISSING_PARAMETERS,
		OK
	}
	private static DeviceType thisDeviceType = DeviceType.INVALID;
	private static DeviceUID thisUID = DeviceUID.fromString("0000000000000000");
	private static byte revICD = 0x00;
	private static KeyProvider keyProvider = null;
	
	final private DeviceUID destinationUID;
	final private IcdHeader headerData;
	final private IcdPayload payload;
	final private MsgStatus msgStatus;

	/***
	 * Call this function before needing to genereate any messages to configure this device 
	 * @param type
	 * @param uid
	 * @param rev
	 */
	public static void configure(DeviceType type, DeviceUID uid, byte rev, KeyProvider keyProv){
		thisDeviceType = type;
		thisUID = uid;
		revICD = rev;
		keyProvider = keyProv;
	}

	/***
	 * Creates an ICD message object from a byte array
	 * If its invalid it will return am empty message with the error code specified
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
	 * Creates a new ICD message based on the payload and destination
	 * Encryption will be performed if nescesary
	 * @param dest
	 * @param msgType
	 * @param payload
	 * @return
	 */
	public static IcdMsg create(SecurityDevice dest, MsgType msgType, Object ... params){
		IcdPayload payload = null;
		DeviceType devType = thisDeviceType;
		
		switch(msgType)
		{
		case DEV_CMD_RESTRICTED:
			devType = DeviceType.DCP;
			if( params.length >= 1)
			{
				if ( dest.devType() == DeviceType.ECOC || dest.devType() == DeviceType.ECM0 )
					payload = RestrictedEcocCmd.create(params);
				else if ( dest.devType() == DeviceType.ACSD || dest.devType() == DeviceType.CSD)
					payload = RestrictedEcocCmd.create(params); //TODO replace with appropiate type
			}
			break;
			
		case DEV_CMD_UNRESTRICTED:
			devType = DeviceType.DCP;
			if( params.length == 1)
				payload = new UnrestrictedCommand((UnrestrictedCmdType)params[0]);
		default:
		}
		//something went wrong building the payload
		if( payload == null)
			return new IcdMsg(MsgStatus.MISSING_PARAMETERS);
		
		IcdHeader headerData = new IcdHeader(
				devType,
				msgType,
				payload.getSize(),
				thisUID,
				revICD,
				dest.getTxAsc());
		
		if( msgType == MsgType.DEV_CMD_UNRESTRICTED)
			((UnrestrictedCommand)payload).putChecksum(headerData);

		return new IcdMsg(dest.UID(),headerData,payload);
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

	/***
	 * Generates the byte array for this message and applies encryption to it if needed	
	 * @return
	 */
	public byte[] getBytes(){
		if( payload == null)
			return null;
		byte []payloadBytes = payload.getBytes();
		if (headerData.msgType.isEncypted()){
			byte[] key = keyProvider.getEncryptionKey(destinationUID);
			payloadBytes = encrypt(payloadBytes,key, headerData.getNonce());
			if ( payloadBytes == null)
				return null;
		}

		byte []headerBytes = headerData.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(headerData.getHdrSize() + payloadBytes.length);
		buffer.put(headerBytes);
		buffer.put(payloadBytes);
		return buffer.array();
	}

//byte chkSum = 0;
//for(byte b : headerBytes)
//	chkSum += b;
//for(byte b : payloadBytes)
//	chkSum += b;
	
	public String toString(){
		if( headerData == null || payload == null)
			return "NO DATA";
		else
			return headerData.toString() + payload.toString();
	}

	private IcdMsg(MsgStatus error){
		this.destinationUID = null;
		this.headerData = null;
		this.payload = null;
		this.msgStatus = error;
	}
	
	private IcdMsg(
			DeviceUID destinationUID,
			IcdHeader headerData,
			IcdPayload payload){
		this.destinationUID = destinationUID;
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
		
		IcdPayload msgContent=null;
		switch(headerData.getMsgType()){
		case UNRESTRICTED_STATUS_MSG:
			msgContent = UnrestrictedStatusMsg.fromBuffer(buffer);
			break;
		case RESTRICTED_STATUS_MSG:
			msgContent = RestrictedStatusMsg.fromBuffer(headerData.getDevType(),buffer);
			break;
		case DEVICE_EVENT_LOG:
			msgContent = EventLogMsg.fromBuffer(headerData.getDevType(),buffer);
			break;
		case NADA_MSG:
			break;
		default:
			MsgType x = headerData.getMsgType();
			return new IcdMsg(MsgStatus.WRONG_SIZE);
			
			// LOG error?
		}

		if( msgContent == null)
			return new IcdMsg(MsgStatus.WRONG_SIZE);

		return new IcdMsg(thisUID, headerData,msgContent);
	}
}	
