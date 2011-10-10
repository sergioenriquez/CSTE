package cste.PacketTypes;

public class DcpPacketTypes {
	public static final short NO_TYPE = 0;
	
	// GENERIC
	public static final short OP_SUCCESS = 5;
	public static final short OP_FAILED = 6;
	
	//dcp to hnad packets
	public static final short HEARTBEAT_REQUEST = 9;
	public static final short CSD_ICD_CONTENT = 10;
	public static final short CHANGE_DCP_ADDRESS = 11;

	//CSD MANAGEMENT CONSOLE PACKETS
	
	public static boolean isValid(short type){
		if ( type >= OP_SUCCESS && type <= CHANGE_DCP_ADDRESS )
			return true;
		else 
			return false;
	}
	
	public static boolean encryptionUsed(short type){
		switch(type){

		case CHANGE_DCP_ADDRESS:
			return true;
			
		default:
			return false;
		}
	}
}
