package cste.icd;

public enum EventLogType {
	NETWORK_DISCOVERY_CHANGED_NAD_CSD 	((byte)0x00,"Net dicovery"),
	CHANGE_IN_STATUS_BY_COMMAND_CSD 	((byte)0x01,"Status change by cmd"),
	CHANGE_IN_STATUS_NOT_BY_COMMAND_CSD ((byte)0x02,"Status change not by cmd"),
	CHANGE_IN_ALARM_STATUS_CSD 			((byte)0x03,"Alarm status change"),
	FAULT_DETECTION_CSD 				((byte)0x04,"Fault detection"),
	
	NETWORK_DISCOVERY_CHANGED_NAD_ECM 	((byte)0x10,"Status change by cmd"),
	CHANGE_IN_STATUS_NOT_BY_COMMAND_ECM ((byte)0x11,"Status change not by cmd"),
	CHANGE_IN_STATUS_BY_COMMAND_ECM 	((byte)0x12,"Status change not by cmd"),
	CHANGE_IN_ALARM_STATUS_ECM 			((byte)0x13,"Alarm status change"),
	FAULT_DETECTION_ECM 				((byte)0x14,"Fault detection"),
	CMD_RECEIVED		 				((byte)0x15,"Cmd Rec"),
	GPS_LOGGGING							((byte)0x16,"GPS Logging"),
	
	INVALID_CMD			 				((byte)0x80,"Invalid Cmd"),
	INVALID 							((byte)0xee,"INVALID"),
	END_OF_RECORDS 						((byte)0xff,"END OF RECORDS");

	private final byte eventType;
	private final String name;
	
	EventLogType(byte type,String name){
		this.eventType = type;
		this.name = name;
	}
	
	public static EventLogType fromValue(byte type){
		for(EventLogType d : EventLogType.values()){
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
