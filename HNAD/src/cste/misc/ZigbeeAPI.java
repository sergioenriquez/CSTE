package cste.misc;

public class ZigbeeAPI {
	private static final int ZIGBEE_OVERHEAD = 10;
	
	public static byte [] makeAPIpkt(byte []msg){
		byte []apiPkt = new byte[msg.length + ZIGBEE_OVERHEAD];
		
		return apiPkt;
	}
}
