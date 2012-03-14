package cste.icd;

//TODO confirm codes are correct
public enum RestrictedCSDCmndType {
	//code, payload size
	NOP 	((byte)0x00,0,(byte)1),
	ACK 	((byte)0x01,1,(byte)2),
	ST	 	((byte)0x02,1,(byte)9),
	CWT		((byte)0x03,1,(byte)17),
	CPI 	((byte)0x04,1,(byte)17),
	WLN 	((byte)0x05,1,(byte)22),
	WLA 	((byte)0x80,1,(byte)22),
	DADC 	((byte)0x81,1,(byte)1),
	DAHH 	((byte)0xA1,1,(byte)1),
	SMAT 	((byte)0xA2,1,(byte)1),
	SMAF 	((byte)0xA3,1,(byte)1),
	SLU 	((byte)0xA4,1,(byte)1),
	EL 		((byte)0xA5,1,(byte)1),
	NES 	((byte)0xA6,1,(byte)1),
	NDS 	((byte)0xA7,1,(byte)1),
	CS 		((byte)0xA8,1,(byte)1),
	RS 		((byte)0xA9,1,(byte)1),
	SP 		((byte)0xAA,1,(byte)1),
	SDQ 	((byte)0xAB,1,(byte)1),
	INVALID	((byte)0xFF,1,(byte)1);
	 
	private final byte typeCode;
	private final byte size;
	private final int paramCnt;

	RestrictedCSDCmndType(byte type,int paramCnt, byte size){
		this.typeCode = type;
		this.size = size;
		this.paramCnt = paramCnt;
	}
	
	public static RestrictedCSDCmndType fromValue(byte type){
		for(RestrictedCSDCmndType d : RestrictedCSDCmndType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	
	public int getParamCnt(){
		return paramCnt;
	}
	
	public byte getSize(){
		return size;
	}
	
	public byte getBytes(){
		return typeCode;
	}
}
