package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;

public class IcdHeader {
	public static final int SECTION_SIZE = 16;
	
	public final DeviceType devType;
	public final MsgType msgType;
	public final byte payloadSize;
	public final DeviceUID devUID;
	public final byte icdRev;
	public int msgAsc;

	public static IcdHeader fromBuffer( ByteBuffer b) {
		if ( b.capacity() >= SECTION_SIZE){
			DeviceType devType = DeviceType.fromValue(b.get());
			MsgType msgType = MsgType.fromValue(b.get());
			byte msgLen = b.get();
			DeviceUID devUID = DeviceUID.fromBuffer(b);
			byte icdRev = b.get();
			int msgAsc = b.getInt();
			
			return new IcdHeader(
					devType,
					msgType,
					msgLen,
					devUID,
					icdRev,
					msgAsc
			);
		}
		else
			return null;
	}
	
	public IcdHeader(
			DeviceType devType,
			MsgType msgType,
			byte msgLen,
			DeviceUID devUID,
			byte icdRev,
			int msgAsc
			){
		this.devType = devType;
		this.msgType = msgType;
		this.payloadSize = msgLen;
		this.devUID = devUID;
		this.icdRev = icdRev;
		this.msgAsc = msgAsc;
	}
	
	/***
	 * Generates the nonce value needed by the encryption function
	 * @return
	 */
	public byte[] getNonce()
	{
		ByteBuffer b = ByteBuffer.allocate(8);
		b.put(devType.getBytes());
		b.put(msgType.getBytes());
		b.put(payloadSize);
		b.put(icdRev);
		b.putInt(msgAsc);
		return b.array();
	}
	
	public byte[] getBytes(){
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(devType.getBytes());
		b.put(msgType.getBytes());
		b.put(payloadSize);
		b.put(devUID.getBytes());
		b.put(icdRev);
		b.putInt(msgAsc);
		
		return b.array();
	}
}
