package cste.icd;

public class DeviceTypes {
	public static final byte KMF = (byte)0x89;
	public static final byte DCP = (byte)0x1;
	public static final byte HNAD = (byte)0x2;
	public static final byte FNAD = (byte)0x3;
	public static final byte CSD = (byte)0x4;
	
	public static int getLevel(byte type){
		switch(type){
		case KMF:
			return 0;
		case DCP:
			return 1;
		case HNAD:
			return 2;
		case FNAD:
			return 2;
		case CSD:
			return 3;
		default:
			return -1;
		}
	}
	
	public static boolean isValid(byte type){
		switch(type){
		case KMF:
		case DCP:
		case HNAD:
		case FNAD:
		case CSD:
			return true;
		default:
			return false;
		}
	}
}
