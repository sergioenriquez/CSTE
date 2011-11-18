package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class IcdHeader {
	public static final int ICD_HEADER_LENGTH = 16;
	
	final DeviceType devType;
	final MsgType msgType;
	final int msgLen;
	final DeviceUID devUID;
	
	final byte icdRev;
	final int msgAsc;
	
	public MsgType getMsgType(){
		return msgType;
	}
	
	public DeviceType getDevType(){
		return devType;
	}
	
	public byte[] getDevUID(){
		return devUID.getBytes();
	}
	
	public int getMsgLen(){
		return msgLen;
	}
	
	public int getIcdRev(){
		return icdRev;
	}
	
	public int getMsgAsc(){
		return msgAsc;
	}
	
	public static IcdHeader fromBytes( byte[] header) {
		if ( header.length == ICD_HEADER_LENGTH){
			ByteBuffer b = ByteBuffer.wrap(header);
			
			DeviceType devType = DeviceType.fromValue(b.get());
			MsgType msgType = MsgType.fromValue(b.get());
			int msgLen = b.getInt();
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
			int msgLen,
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
	
	public byte[] getBytes(){
		ByteBuffer b = ByteBuffer.allocate(ICD_HEADER_LENGTH);
		b.put(devType.toByte());
		b.put(msgType.toByte());
		b.putInt(msgLen);
		b.put(devUID.getBytes());
		b.put(icdRev);
		b.putInt(msgAsc);
		
		return b.array();
	}
}
