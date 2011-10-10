package cste.messages;

public enum MsgType{
	UNRESTRICTED_STATUS_MSG		(0x00),
	// 0x01 - 0x7F RESERVED
	RESTRICTED_STATUS_MSG		(0x80),
	DEVICE_EVENT_LOG			(0x81),
	SENSOR_DISCOVERY_BROADCAST	(0xA0),
	SENSOR_DATABASE_REPORT		(0xA1),
	SENSOR_STATUS_MSG			(0xA2),
	AOS_TO_DEV_SENSOR_CFG_MSG	(0xA3),
	DEV_TO_NAD_SENSOR_CFG_MFG	(0xA4),
	// 0xA5 - 0xAF reserved for aos
	DEV_CMD_RESTRICTED			(0xC0),
	DEV_CMD_UNRESTRICTED		(0xC1),
	DEV_TO_SENSOR_RESTRICTED_CMD(0xC2),
	ENCRYPYION_REKEY			(0xE0),
	// 0xE1 - 0xE9 reserved for key management
	NADA_MSG					(0x00),
	INVALID						(0xFF);
	
	
	private final int typeCode;
	
	public static MsgType fromInt(int type){
		for(MsgType d : MsgType.values()){
			 if ( d.toByte() == type)
				 return d;
		}
		return INVALID;
	}
	
	public byte toByte(){
		return (byte)typeCode;
	}
	
	MsgType(int type){
		this.typeCode = type;
	}
}

