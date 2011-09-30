package cste.icd;

public class DeviceTypes {
	public static final byte KMF = (byte)0x89;
	public static final byte DCP = (byte)0x88;
	public static final byte HNAD = (byte)0x87; //TODO confirm this the correct device type
	public static final byte FNAD = (byte)0x86;
	public static final byte CSD = (byte)0x85;
	
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
