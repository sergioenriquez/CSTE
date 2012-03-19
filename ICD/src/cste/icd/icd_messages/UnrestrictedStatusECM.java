package cste.icd.icd_messages;

import java.nio.ByteBuffer;

import cste.icd.general.IcdPayload;
import cste.icd.types.DeviceUID;

public class UnrestrictedStatusECM extends IcdPayload {
	public static final int SECTION_SIZE = 15;
	public byte deviceSectionCode;
	public byte encryptionErrorCode;
	public int rekeyCtr;
	public DeviceUID senderUID;
	public byte ascNumber;

	public UnrestrictedStatusECM(ByteBuffer b){
		deviceSectionCode =  b.get();
		encryptionErrorCode =  b.get();
		rekeyCtr = b.getInt();
		senderUID = DeviceUID.fromBuffer(b);
		ascNumber = b.get();
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(deviceSectionCode);
		b.put(encryptionErrorCode);
		b.putInt(rekeyCtr);
		b.put(senderUID.getBytes());
		b.put(ascNumber);
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return SECTION_SIZE;
	}
}
