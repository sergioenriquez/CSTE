package cste.icd;

public enum NadEventLogType {
	POWER_ON 			((byte)0x01,"Power ON"),
	POWER_OFF 			((byte)0x02,"Power OFF"),
	LOGIN_SUCCESS 		((byte)0x03,"Login OK"),
	LOGIN_FAILURE 		((byte)0x04,"Login ERROR"),
	ICD_MSG_RECEIVED	((byte)0x05,"ICD Msg"),
	KEYS_RETRIEVED 		((byte)0x06,"Keys Rec"),
	HARDWARE_FAULURE	((byte)0x06,"Hw Error"),
	INVALID				((byte)0xFF,"N/A");
	
	private final byte eventType;
	private final String name;
	
	NadEventLogType(byte type,String name){
		this.eventType = type;
		this.name = name;
	}
	
	public static NadEventLogType fromValue(byte type){
		for(NadEventLogType d : NadEventLogType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	
	public byte getBytes(){
		return eventType;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
