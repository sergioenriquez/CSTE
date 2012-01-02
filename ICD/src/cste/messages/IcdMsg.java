package cste.messages;


public class IcdMsg {
	
	IcdHeader headerData = null;
	Object msgContent = null;
	int error = 0;
	byte[] byteContent = null;
	
	public IcdMsg(byte[] messageBytes){
		parseBytes(messageBytes);
	}

	public MsgType getMsgType(){
		return headerData.getMsgType();
	}

	public IcdHeader getHeader(){
		return headerData;
	}
	
	public void parseBytes(byte[] messageBytes){
		headerData = IcdHeader.fromBytes(messageBytes);
		if ( headerData == null){
			error = 1; // cannot parse
			return;
		}
		
		switch(headerData.getMsgType()){
		case UNRESTRICTED_STATUS_MSG:
			//msgContent = RestrictedStatusMsg.fromBytes(messageBytes);
			break;
		case RESTRICTED_STATUS_MSG:
			msgContent = RestrictedStatusMsg.fromBytes(headerData.getDevType(),messageBytes);
			break;
		case DEVICE_EVENT_LOG:
			msgContent = EventLogMsg.fromBytes(headerData.getDevType(),messageBytes);
			break;
		case NADA_MSG:
			break;
		default:
			msgContent = null;
		}
		
		this.byteContent = messageBytes.clone();
	}

	public String toString(){
		return headerData.toString() + msgContent.toString();
	}
	
	public Object getContent(){
		
		return msgContent;
	}
	
	
}	
