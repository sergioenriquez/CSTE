package cste.messages;

public class IcdHeader {
	MsgType msgType;
	
	public MsgType getMsgType(){
		return msgType;
	}
	
	public IcdHeader(byte[] header) {
	}
}
