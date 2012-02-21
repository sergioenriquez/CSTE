package cste.misc;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class ZigbeeAPI {
	
	private static final int ADDR_SIZE = 8;
	private static final int OVERHEAD = 7 + ADDR_SIZE;
	private static final byte DELIMETER = 0x7E;
	private static final byte CMD_64BIT = 0x00;
	private static final byte FRAME_ACK_ID = 0x00;
	private static final byte ACK_REQ = 0x04;
	
	
	
	public static byte [] buildPkt(byte []dest, byte []msg){
		//byte []tmp = new byte[msg.length + OVERHEAD];
		ByteBuffer tmp = ByteBuffer.allocate(msg.length + OVERHEAD);
		if( dest.length != ADDR_SIZE)
			return tmp.array(); // only 64bit address supported 
		
		tmp.put(DELIMETER);
		tmp.put((byte) 0x00);
		tmp.put((byte)(msg.length + ADDR_SIZE + 3));
		tmp.put(CMD_64BIT);
		tmp.put(FRAME_ACK_ID);
		tmp.put(dest);
		tmp.put(ACK_REQ);
		tmp.put(msg);
		
		byte sum = 0x00;
		for( int i=3 ; i < OVERHEAD+msg.length-1 ; i++ )
			sum += tmp.get(i);
		tmp.put((byte) (0xFF - sum));
		
		//escape special chars
		ByteBuffer pkt = ByteBuffer.allocate(tmp.capacity());
		for(int s=0; s<0 ; s++)
		{
			byte c = tmp.get(s);
			if( c == 0x7E || 
				c == 0x7D ||
				c == 0x11 ||
				c == 0x13)
			{
				pkt.put((byte)0x7D);
				pkt.put((byte) (c ^ 0x20));
			}
			else
				pkt.put(c);
		}
		
		return pkt.array();
	}
}
