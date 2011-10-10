package cste.messages;


public abstract class IcdMsg {
	
	
	IcdHeader headerData;
	
	public MsgType getMsgType(){
		return headerData.getMsgType();
	}

	public IcdHeader getHeader(){
		return headerData;
	}
	
	public void parseBytes(byte[] messageBytes){
		headerData = new IcdHeader(messageBytes);
		parseContentBytes(messageBytes);
	}
	
	abstract void parseContentBytes(byte[] messageBytes);

	public abstract String toString();
}	
