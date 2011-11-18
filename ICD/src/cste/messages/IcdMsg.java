package cste.messages;


public class IcdMsg {
	
	IcdHeader headerData;
	Object msgContent = null;
	
	public MsgType getMsgType(){
		return headerData.getMsgType();
	}

	public IcdHeader getHeader(){
		return headerData;
	}
	
	public void parseBytes(byte[] messageBytes){
		headerData = IcdHeader.fromBytes(messageBytes);
		
		switch(headerData.getMsgType()){
		case UNRESTRICTED_STATUS_MSG:
			msgContent = CsdRestrictedStatusMsg.fromBytes(messageBytes);
			break;
		case RESTRICTED_STATUS_MSG:
			msgContent = CsdUnrestrictedStatusMsg.fromBytes(messageBytes);
			break;
		case DEVICE_EVENT_LOG:
			msgContent = EventLogMsg.fromBytes(headerData.getDevType(),messageBytes);
			break;
		default:
			msgContent = null;
		}
	}

	public String toString(){
		return headerData.toString() + msgContent.toString();
	}
	
	public Object getContent(){
		
		return msgContent;
	}
	
	
}	
