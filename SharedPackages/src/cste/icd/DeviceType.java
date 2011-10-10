package cste.icd;

public enum DeviceType {
	INVALID (0x00),
	HNAD_N 	(0x92),
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

	public static DeviceType fromInt(int type){
		for(DeviceType d : DeviceType.values()){
			 if ( d.toByte() == type)
				 return d;
		}
		return INVALID;
	}
	 
	public byte toByte(){
		return (byte)typeCode;
	}
	
	public int getLevel(){
		switch(this){
		case KMF:
			return 0;
		case DCP:
			return 1;
		case HNAD_N:
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
