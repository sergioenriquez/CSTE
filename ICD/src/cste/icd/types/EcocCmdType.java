package cste.icd.types;

public enum EcocCmdType {
	//code, param count, payload size
	NOP 	((byte)0x00,0,(byte)1),
	ACK 	((byte)0x01,1,(byte)2),
	ST	 	((byte)0x03,1,(byte)9),
	CWT		((byte)0x04,1,(byte)17),
	CPI 	((byte)0x05,1,(byte)17),
	WLN 	((byte)0x06,1,(byte)21),
	WLA 	((byte)0x07,1,(byte)21),
	DADC 	((byte)0x80,0,(byte)1),
	DAHH 	((byte)0x81,0,(byte)1),
	SMAT 	((byte)0xA1,0,(byte)1),
	SMAF 	((byte)0xA2,0,(byte)1),
	SLU 	((byte)0xA3,0,(byte)1),
	SL 		((byte)0xA4,0,(byte)1),
	EL	 	((byte)0xA5,0,(byte)1),
	INVALID	((byte)0xFF,0,(byte)1);
	 
	private final byte typeCode;
	private final byte size;
	private final int paramCnt;

	EcocCmdType(byte type,int paramCnt, byte size){
		this.typeCode = type;
		this.size = size;
		this.paramCnt = paramCnt;
	}
	
	public static EcocCmdType fromValue(byte type){
		for(EcocCmdType d : EcocCmdType.values()){
			 if ( d.getBytes() == type)
				 return d;
		}
		return INVALID;
	}
	
	public byte getSize(){
		return size;
	}
	
	public byte getBytes(){
		return typeCode;
	}
	
	public int getParamCnt(){
		return paramCnt;
	}
}
