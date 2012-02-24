package cste.messages;

import java.nio.ByteBuffer;


public class IcdMsg {
	public enum MsgStatus{
		BAD_CHEKSUM,
		WRONG_SIZE,
		EMPTY_MSG,
		OTHER,
		OK
	}
	
	final private IcdHeader headerData;
	final private Object msgContent;
	final private MsgStatus msgStatus;
	final private byte[] byteContent;
	
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

	public MsgType getMsgType(){
		return headerData.getMsgType();
	}

	public IcdHeader getHeader(){
		return headerData;
	}
	
	public MsgStatus getStatus(){
		return msgStatus;
	}
	
	public String toString(){
		if( headerData == null || msgContent == null)
			return "NO DATA";
		else
			return headerData.toString() + msgContent.toString();
	}
	
	public Object getContent(){
		return msgContent;
	}
	
	public byte[] getBytes(){
		return byteContent;
	}
	
	private IcdMsg(MsgStatus error){
		this.headerData = null;
		this.msgContent = null;
		this.byteContent = null;
		this.msgStatus = error;
	}
	
	private IcdMsg(
			IcdHeader headerData,
			Object msgContent,
			byte[] byteContent){
		this.headerData = headerData;
		this.msgContent = msgContent;
		this.byteContent = byteContent;
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
		
		Object msgContent=null;
		switch(headerData.getMsgType()){
		case UNRESTRICTED_STATUS_MSG:
			msgContent = UnrestrictedStatusMsg.fromBuffer(buffer);
			break;
		case RESTRICTED_STATUS_MSG:
			msgContent = RestrictedStatusMsg.fromBytes(headerData.getDevType(),data);
			break;
		case DEVICE_EVENT_LOG:
			msgContent = EventLogMsg.fromBytes(headerData.getDevType(),data);
			break;
		case NADA_MSG:
			break;
		default:
			// LOG error?
		}
		
		if( msgContent == null)
			return new IcdMsg(MsgStatus.WRONG_SIZE);
		
		return new IcdMsg(headerData,msgContent,data);
	}
}	
