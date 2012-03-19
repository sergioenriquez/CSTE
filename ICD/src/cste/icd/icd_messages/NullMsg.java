package cste.icd.icd_messages;

import java.nio.ByteBuffer;

import cste.icd.general.IcdPayload;
import cste.icd.types.DeviceUID;

public class NullMsg extends IcdPayload{
	public static final int SECTION_SIZE = 10;
	byte devType;
	DeviceUID devUID;
	byte checkSum;
	
	public NullMsg(ByteBuffer b) {
		devType = b.get();
		devUID = DeviceUID.fromBuffer(b);
		checkSum = b.get();
	}
	
	@Override
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(devType);
		b.put(devUID.getBytes());
		b.put(checkSum);
		return b.array();
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public byte getSize() {
		return SECTION_SIZE;
	}
}
