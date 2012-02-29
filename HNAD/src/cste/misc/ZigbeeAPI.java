package cste.misc;

import java.nio.ByteBuffer;

import android.util.Log;

public class ZigbeeAPI {
	private static final String TAG = "Zigbee API";
	
	private static final int ADDR_SIZE = 8;
	private static final int OVERHEAD = 7 + ADDR_SIZE;
	private static final byte DELIMETER = 0x7E;
	private static final byte CMD_64BIT = 0x00;
	
	public static final byte RX_64BIT = (byte) 0x80;
	public static final byte RX_16BIT = (byte) 0x81;
	public static final byte TX_STATUS = (byte) 0x89;

	private static final byte NO_ACK = 0x01;
	private static final byte ACK_REQ = 0x00;
	
	/***
	 * Based on API level 2, supports 64 bit addresses with no ACK
	 * @param dest
	 * @param msg
	 * @return
	 */
	public static byte [] buildPkt(byte []dest, byte frameID, byte []msg){
		ByteBuffer tmp = ByteBuffer.allocate(msg.length + OVERHEAD);
		if( dest.length != ADDR_SIZE)
			return tmp.array(); // only 64bit address supported 

		tmp.put(CMD_64BIT);
		tmp.put(frameID);
		tmp.put(dest);
		if( frameID == 0)
			tmp.put(NO_ACK);
		else
			tmp.put(ACK_REQ);
		tmp.put(msg);
		
		byte sum = 0x00;
		for( int i=0 ; i < OVERHEAD+msg.length-1 ; i++ )
			sum += tmp.get(i);
		tmp.put((byte) (0xFF - sum));
		
		//escape special chars
		ByteBuffer escapedSeq = ByteBuffer.allocate(tmp.capacity()*2);
		int newSize=0;
		escapedSeq.put(DELIMETER);
		escapedSeq.put((byte) 0x00);
		escapedSeq.put((byte)(msg.length + ADDR_SIZE + 3));
		for(int s=0; s<tmp.capacity() ; s++,newSize++)
		{
			byte c = tmp.get(s);
			if( c == 0x7E || 
				c == 0x7D ||
				c == 0x11 ||
				c == 0x13)
			{
				escapedSeq.put((byte)0x7D);
				escapedSeq.put((byte) (c ^ 0x20));
				newSize++;
			}
			else
				escapedSeq.put(c);
		}
		byte []zigbeePkt = new byte[newSize];
		escapedSeq.rewind();
		escapedSeq.get(zigbeePkt);
		
		return zigbeePkt;
	}
	

	
	
	//TODO handle other msg types
	/***
	 * Unwraps the data delivered by the Zigbee transceiver
	 * @param msg
	 * @return
	 */
	public static ZigbeeFrame parsePkt(byte[] msg)
	{
		ByteBuffer temp = ByteBuffer.wrap(msg);
		ByteBuffer data = ByteBuffer.allocate(temp.capacity());
		for(int i=0;i<temp.capacity();i++)
		{
			byte c = temp.get();
			if(	c == 0x7D )
			{
				c = temp.get();
				c ^= 0x20;
				i++;
				data.put(c);
			}
			else
				data.put(c);
		}
		data.rewind();
		data.get();//delimeter
		short frameSize = data.getShort();
		byte type = data.get();
		
		short addrSize;
		if(type == RX_64BIT)
			addrSize = 8;
		else if(type == RX_16BIT)
			addrSize = 2;
		else if( type == TX_STATUS)
		{
			byte frameACK = data.get();
			byte txStatus = data.get();
			return new ZigbeeFrame(type,frameACK,txStatus);
		}
		else
		{
			Log.w(TAG, "Zigbee packet type not known");
			return new ZigbeeFrame(type);
		}
		
		byte[] source = new byte[addrSize];
		data.get(source);
		
		int rssi = data.get();
		byte opt = data.get();
		
		short payloadSize = (short) (frameSize-addrSize-3);
		if( payloadSize > 0)
		{
			byte[] payload = new byte[payloadSize];
			data.get(payload);
			return new ZigbeeFrame(type,rssi,opt,source,payload);
		}
		else
		{
			 Log.w(TAG, "Bad frame size received");
			 return new ZigbeeFrame(type);
		}
	}
}
