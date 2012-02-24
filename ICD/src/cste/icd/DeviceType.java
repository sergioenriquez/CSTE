package cste.icd;

public enum DeviceType {
	INVALID (0x00),
	FNAD_N 	(0x92),
	HNAD_A	(0x91),
	HNAD_I	(0x90),	
	KMF		(0x89),
	DCP  	(0x88),
	FNAD_S	(0x87),
	HNAD_S	(0x86),
	FNAD_I	(0x85),
	ECM0  	(0x84),
	WAOS	(0x83),
	ECOC	(0x82),
	CSD		(0x81),
	ACSD	(0x80);
	 
	private final int typeCode;
	 
	DeviceType(int type){
		this.typeCode = type;
	}

	public static DeviceType fromValue(int type){
		for(DeviceType d : DeviceType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	 	
	public byte getBytes(){
		return (byte)typeCode;
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

	@Override
	public String toString(){
		switch(this){
		case KMF:
			return "KMF";
		case DCP:
			return "DCP";
		case FNAD_N:
			return "FNAD Non-Root";
		case HNAD_A:
			return "HNAD Arming only";
		case HNAD_I:
			return "HNAD Non-secure";
		case HNAD_S:
			return "HNAD Secure";
		case FNAD_S:
			return "FNAD Secure";
		case FNAD_I:
			return "FNAD Non-secure";
		case ECM0:
			return "ECM Type 0";
		case WAOS:
			return "Wireless AoS";
		case ECOC:
			return "ECoC";
		case CSD:
			return "CSD";
		default:
			return "NA";
		}
	}
}
