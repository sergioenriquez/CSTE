package cste.messages;

import java.nio.ByteBuffer;

public abstract class IcdPayload {
	
	public static IcdPayload fromBuffer(int type, ByteBuffer buffer){
		return CsdLogDeviceStatus.fromBuffer(buffer);
	}
	
	public abstract byte[] getBytes();
	public abstract String toString();
	public abstract byte getSize();
}
