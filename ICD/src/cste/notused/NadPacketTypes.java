package cste.notused;

public class NadPacketTypes {
	public static final short NO_TYPE = 0;
	
	// GENERIC
	public static final short OP_SUCCESS = 5;
	public static final short OP_FAILED = 6;

	//kmf to dcp packets
	public static final short REPLY_KEY = 7;

	//NAD to dcp packets
	public static final short HEARTBEAT_RESPONSE = 12;
	public static final short NAD_ICD_CONTENT = 13;
	public static final short LOGIN_REQUEST = 14;
	
	//CSD MANAGEMENT CONSOLE PACKETS
	
	public static boolean isValid(short type){
		if ( type >= OP_SUCCESS && type <= LOGIN_REQUEST )
			return true;
		else 
			return false;
	}
	
	public static boolean encryptionUsed(short type){
		switch(type){
		case REPLY_KEY:
		case LOGIN_REQUEST:
			return true;
			
		default:
			return false;
		}
	}
}
