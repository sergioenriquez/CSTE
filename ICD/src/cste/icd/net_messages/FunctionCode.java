package cste.icd.net_messages;

public enum FunctionCode {
	//ICD
	HEARTBEAT_REQUEST	((byte) 0x00),
	HEARTBEAT_REPLY		((byte) 0x01),
	DCP_CONFIG_CHANGE	((byte) 0x02),
	ICD_MSG_NAD			((byte) 0x0A),
	ICD_MSG_DCP			((byte) 0x0B),
	//Non ICD
	LOGIN_REQUEST		((byte) 0xE1),
	LOGIN_REPLY			((byte) 0xE2),
	KEY_REQUEST			((byte) 0xE3),
	KEY_REPLY			((byte) 0xE4),
	INVALID				((byte) 0xFF);
	
	private final byte eventType;
	
	FunctionCode(byte type){
		this.eventType = type;
	}
	
	public byte getBytes(){
		return eventType;
	}
	
	public static FunctionCode fromValue(int type){
		for(FunctionCode d : FunctionCode.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
}
