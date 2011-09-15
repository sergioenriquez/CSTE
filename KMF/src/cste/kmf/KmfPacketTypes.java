package cste.kmf;

/*
 * 
 */

public class KmfPacketTypes {
	static final int ADD_RECORD = 1;
	static final int DELETE_RECORD = 2;
	static final int GENERATE_LTK = 3;
	static final int GENERATE_TCK = 4;
	
	static public class AddRecordPacket{
		public byte type;
		public byte uid[] = new byte[4];
		public byte rekeyKey[] = new byte[16];
	}
}
