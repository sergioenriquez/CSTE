package cste.ip;

import static cste.ip.PacketTypes.ADD_RECORD;
import static cste.ip.PacketTypes.CHANGE_DCP_ADDRESS;
import static cste.ip.PacketTypes.LOGIN_REQUEST;
import static cste.ip.PacketTypes.REPLY_KEY;

/***
 * Represents allowed packet types that KMF server can handle
 * @author Sergio Enriquez
 *
 */
public class PacketTypes {
	public static final short NO_TYPE = 0;
	
	// GENERIC
	public static final short OP_SUCCESS = 5;
	public static final short OP_FAILED = 6;
	
	//dcp to kmf packets
	public static final short ADD_RECORD = 1;
	public static final short DELETE_RECORD = 2;
	public static final short GENERATE_LTK = 3;
	public static final short GENERATE_TCK = 4;
	
	//kmf to dcp packets
	public static final short REPLY_KEY = 7;
	
	//dcp to hnad packets
	public static final short HEARTBEAT_REQUEST = 9;
	public static final short CSD_ICD_CONTENT = 10;
	public static final short CHANGE_DCP_ADDRESS = 11;
	
	//NAD to dcp packets
	public static final short HEARTBEAT_RESPONSE = 12;
	public static final short NAD_ICD_CONTENT = 13;
	public static final short LOGIN_REQUEST = 14;
	
	//CSD MANAGEMENT CONSOLE PACKETS
	
	public static boolean isValid(short type){
		if ( type >= ADD_RECORD && type <= GENERATE_TCK )
			return true;
		else 
			return false;
	}
	
	public static boolean encryptionUsed(short type){
		switch(type){
		case ADD_RECORD:
		case REPLY_KEY:
		case LOGIN_REQUEST:
		case CHANGE_DCP_ADDRESS:
			return true;
			
		default:
			return false;
		}
	}
}
