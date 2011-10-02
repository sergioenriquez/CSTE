package cste.ip;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

public class IcdIpPacket {
	byte rev;
	short functionCode;
	byte[] senderUID;
	byte[] payload;
	
	public void setRev(byte rev){
		this.rev = rev;
	}
	
	public void setFunctionCode(short functionCode){
		this.functionCode = functionCode;
	}

	public void setSenderUID(byte[] senderUID){
		this.senderUID = senderUID;
	}
	
	public byte[] getSenderUID(){
		return senderUID;
	}
	
	public void setPayload(byte[] payload){
		this.payload =	payload;
	}
	
	public byte[] getPayload(){
		return payload;
	}
	
	public DataInputStream getPayloadDataStream(){
		InputStream s = new ByteArrayInputStream(getPayload());
		DataInputStream b = new DataInputStream(s);
		return b;
	}
	
	public short getFunctionCode(){
		return functionCode;
	}
}
