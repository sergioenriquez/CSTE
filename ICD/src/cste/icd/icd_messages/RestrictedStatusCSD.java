package cste.icd.icd_messages;

import java.nio.ByteBuffer;

//TODO implement class
public class RestrictedStatusCSD extends RestrictedStatus{
	private static final long serialVersionUID = 2408917513174727564L;
	public static final int SECTION_SIZE = 52;

	public RestrictedStatusCSD(ByteBuffer b) {
		errorCode = b.get();
		restrictedDataSection = new byte[SECTION_SIZE-1];
		b.get(restrictedDataSection);
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(errorCode);
		b.put(restrictedDataSection);
		return b.array();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		return SECTION_SIZE;
	}
}
