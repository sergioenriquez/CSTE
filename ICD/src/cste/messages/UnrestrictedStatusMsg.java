package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class UnrestrictedStatusMsg {
	public static final int SECTION_SIZE = 15;
	private DeviceType type;
	public byte deviceSectionCode;
	
	public byte encryptionErrorCode;
	public int rekeyCtr;
	public DeviceUID senderUID;
	public byte ascNumber;
	public byte checksum;
	
	
	public static UnrestrictedStatusMsg fromBytes(byte[] content) {
		
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH , SECTION_SIZE);
		byte deviceSectionCode = b.get();
		byte encryptionErrorCode = b.get();
		int rekeyCtr = b.getInt();
		DeviceUID senderUID = DeviceUID.fromBuffer(b);
		
		byte ascNumber = b.get();
		byte checksum = b.get();

		return new UnrestrictedStatusMsg(
				deviceSectionCode,
				encryptionErrorCode,
				rekeyCtr,
				senderUID,
				ascNumber,
				checksum);
	}
	
	public UnrestrictedStatusMsg(
			byte deviceSectionCode,
			byte encryptionErrorCode,
			int  rekeyCtr,
			DeviceUID senderUID,
			byte ascNumber,
			byte checksum
			){
		this.deviceSectionCode = deviceSectionCode;
		this.encryptionErrorCode = encryptionErrorCode;
		this.rekeyCtr = rekeyCtr;
		this.senderUID = senderUID;
		this.ascNumber = ascNumber;
		this.checksum = checksum;
	}
}
