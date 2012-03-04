package cste.icd;

public enum NadaTimeDelay {
	D20		((byte)0x00,20),
	D40		((byte)0x01,40),
	D80		((byte)0x02,80),
	D100	((byte)0x03,100),
	D200	((byte)0x04,200),
	D400	((byte)0x05,400),
	D800	((byte)0x06,800),
	D1000	((byte)0x07,1000);
	
	byte code;
	int msDelay;
	
	NadaTimeDelay(byte code, int msDelay){
		this.code = code;
		this.msDelay = msDelay;
	}
	
	public byte getBytes(){
		return code;
	}
	
	public int getMsDelay(){
		return msDelay;
	}
}
