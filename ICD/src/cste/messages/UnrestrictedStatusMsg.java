package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class UnrestrictedStatusMsg extends IcdPayload {
	public static final int SECTION_SIZE = 15;
	private DeviceType type;
	public byte deviceSectionCode;
	
	public byte encryptionErrorCode;
	public int rekeyCtr;
	public DeviceUID senderUID;
	public byte ascNumber;

	public static UnrestrictedStatusMsg fromBuffer(ByteBuffer b) {
		if ( b.remaining() < SECTION_SIZE )
			return null;
		
		byte deviceSectionCode = b.get();
		byte encryptionErrorCode = b.get();
		int rekeyCtr = b.getInt();
		DeviceUID senderUID = DeviceUID.fromBuffer(b);
		byte ascNumber = b.get();
	
		return new UnrestrictedStatusMsg(
				deviceSectionCode,
				encryptionErrorCode,
				rekeyCtr,
				senderUID,
				ascNumber
				);
	}
	
	public UnrestrictedStatusMsg(
			byte deviceSectionCode,
			byte encryptionErrorCode,
			int  rekeyCtr,
			DeviceUID senderUID,
			byte ascNumber){
		this.deviceSectionCode = deviceSectionCode;
		this.encryptionErrorCode = encryptionErrorCode;
		this.rekeyCtr = rekeyCtr;
		this.senderUID = senderUID;
		this.ascNumber = ascNumber;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
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
		return 0;
	}
}
