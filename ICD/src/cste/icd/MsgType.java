package cste.icd;

public enum MsgType{			//code, isEncrypted
	UNRESTRICTED_STATUS_MSG		((byte)0x00,false),
	// 0x01 - 0x7F RESERVED
	RESTRICTED_STATUS_MSG		((byte)0x80,true),
	DEVICE_EVENT_LOG			((byte)0x81,true),
	SENSOR_DISCOVERY_BROADCAST	((byte)0xA0,true),
	SENSOR_DATABASE_REPORT		((byte)0xA1,true),
	SENSOR_STATUS_MSG			((byte)0xA2,true),
	AOS_TO_DEV_SENSOR_CFG_MSG	((byte)0xA3,true),
	DEV_TO_NAD_SENSOR_CFG_MFG	((byte)0xA4,true),
	// 0xA5 - 0xAF reserved for aos
	DEV_CMD_UNRESTRICTED		((byte)0xC0,false),
	DEV_CMD_RESTRICTED			((byte)0xC1,true),
	DEV_TO_SENSOR_RESTRICTED_CMD((byte)0xC2,true),
	ENCRYPYION_REKEY			((byte)0xE0,true),
	NULL_MSG					((byte)0x82,false),
	// 0xE1 - 0xE9 reserved for key management
	NADA_MSG					((byte)0xFF,false),
	INVALID						((byte)0xFE,false);
	
	private final byte typeCode;
	private final boolean isEncripted;
	
	MsgType(byte type, boolean isEncripted){
		this.typeCode = type;
		this.isEncripted = isEncripted;
	}
	
	public boolean isEncypted(){
		return isEncripted;
	}
	
	public static MsgType fromValue(int type){
		for(MsgType d : MsgType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	
	public byte getBytes(){
		return (byte)typeCode;
	}
}

