package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.MsgType;

public class IcdHeader {
	public static final int ICD_HEADER_LENGTH = 16;
	public static final int ICD_NONCE_LENGTH = 8;
	
	final DeviceType devType;
	final MsgType msgType;
	final byte msgLen;
	final DeviceUID devUID;
	
	final byte icdRev;
	final int msgAsc;
	
	public MsgType getMsgType(){
		return msgType;
	}
	
	public DeviceType getDevType(){
		return devType;
	}
	
	public DeviceUID getDevUID(){
		return devUID;
	}
	
	public int getPayloadSize(){
		return msgLen;
	}
	
	public int getHdrSize(){
		return ICD_HEADER_LENGTH;
	}
	
	public int getIcdRev(){
		return icdRev;
	}
	
	public int getMsgAsc(){
		return msgAsc;
	}
	
	public static IcdHeader fromBuffer( ByteBuffer b) {
		if ( b.capacity() >= ICD_HEADER_LENGTH){
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
		this.msgLen = msgLen;
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
		b.put(msgLen);
		b.put(icdRev);
		b.putInt(msgAsc);
		return b.array();
	}
	
	public byte[] getBytes(){
		ByteBuffer b = ByteBuffer.allocate(ICD_HEADER_LENGTH);
		b.put(devType.getBytes());
		b.put(msgType.getBytes());
		b.put(msgLen);
		b.put(devUID.getBytes());
		b.put(icdRev);
		b.putInt(msgAsc);
		
		return b.array();
	}
}
