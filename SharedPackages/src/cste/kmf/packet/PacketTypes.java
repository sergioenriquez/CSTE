package cste.kmf.packet;

/***
 * Represents allowed packet types that KMF server can handle
 * @author Sergio Enriquez
 *
 */
public class PacketTypes {
	public static final byte NO_TYPE = 0;
	public static final byte ADD_RECORD = 1;
	public static final byte DELETE_RECORD = 2;
	public static final byte GENERATE_LTK = 3;
	public static final byte GENERATE_TCK = 4;
	
	public static boolean isValid(byte type){
		if ( type >= ADD_RECORD && type <= GENERATE_TCK )
			return true;
		else 
			return false;
	}
}
