package cste.icd.types;

public enum DeviceType {
	INVALID ((byte)0x00,"Invalid Type"),
	FNAD_N 	((byte)0x92,"FNAD Non-Root"),
	HNAD_A	((byte)0x91,"HNAD Arming only"),
	HNAD_I	((byte)0x90,"HNAD Non-secure"),	
	KMF		((byte)0x89,"KMF"),
	DCP  	((byte)0x88,"DCP"),
	FNAD_S	((byte)0x87,"FNAD Secure"),
	HNAD_S	((byte)0x86,"HNAD Secure"),
	FNAD_I	((byte)0x85,"FNAD Non-Secure"),
	ECM0  	((byte)0x84,"ECM Type 0"),
	WAOS	((byte)0x83,"Wireless AoS"),
	ECOC	((byte)0x82,"ECoC"),
	CSD		((byte)0x81,"CSD"),
	ACSD	((byte)0x80,"ACSD");
	
	private final byte typeCode;
	private final String name;
	 
	DeviceType(byte type, String name){
		this.typeCode = type;
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}

	public static DeviceType fromValue(int type){
		for(DeviceType d : DeviceType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	 	
	public byte getBytes(){
		return typeCode;
	}
	
	public int getLevel(){
		switch(this){
		case KMF:
			return 0;
		case DCP:
			return 1;
		case FNAD_N:
		case HNAD_A:
		case HNAD_I:
		case HNAD_S:
		case FNAD_S:
		case FNAD_I:
			return 2;
		case ECM0:
		case WAOS:
		case ECOC:
		case CSD:
			return 3;
		default:
			return -1;
		}
	}
}
