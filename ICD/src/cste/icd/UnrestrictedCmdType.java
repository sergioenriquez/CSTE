package cste.icd;

//TODO confirm codes are correct
public enum UnrestrictedCmdType{
	//code, payload size
	REQUEST	((byte)0x00),
	PAIR 	((byte)0x01),
	PAIR_REQ((byte)0x02),
	INVALID ((byte)0xFF);
	 
	private final byte typeCode;

	UnrestrictedCmdType(byte type){
		this.typeCode = type;
	}
	
	public static UnrestrictedCmdType fromValue(byte type){
		for(UnrestrictedCmdType d : UnrestrictedCmdType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}

	public byte getBytes(){
		return typeCode;
	}
}
