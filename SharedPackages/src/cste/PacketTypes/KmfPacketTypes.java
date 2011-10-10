package cste.PacketTypes;


/***
 * Represents allowed packet types that KMF server can handle
 * @author Sergio Enriquez
 *
 */
public class KmfPacketTypes {
	public static final short NO_TYPE = 0;
	
	// GENERIC
	public static final short OP_SUCCESS = 5;
	public static final short OP_FAILED = 6;
	public static final short ADD_RECORD = 1;
	public static final short DELETE_RECORD = 2;
	public static final short GENERATE_LTK = 3;
	public static final short GENERATE_TCK = 4;
	public static final short REPLY_KEY = 7;
	
	//CSD MANAGEMENT CONSOLE PACKETS
	
	public static boolean isValid(short type){
		if ( type >= ADD_RECORD && type <= REPLY_KEY )
			return true;
		else 
			return false;
	}
	
	public static boolean encryptionUsed(short type){
		switch(type){
		case ADD_RECORD:
		case REPLY_KEY:
			return true;
		default:
			return false;
		}
	}
}
