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
		return (byte)(code << 4  | 0x01);
	}
	
	public int getMsDelay(){
		return msDelay;
	}
	
	public static NadaTimeDelay fromIndex(int index){
		switch(index){
		case 0:
			return NadaTimeDelay.D20;
		case 1:
			return NadaTimeDelay.D40;
		case 2:
			return NadaTimeDelay.D80;
		case 3:
			return NadaTimeDelay.D100;
		case 4:
			return NadaTimeDelay.D200;
		case 5:
			return NadaTimeDelay.D400;
		case 6:
			return NadaTimeDelay.D800;
		case 7:
			return NadaTimeDelay.D1000;
		default :
			return NadaTimeDelay.D20;
		}
	}
}
