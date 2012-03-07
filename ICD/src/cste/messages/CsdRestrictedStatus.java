package cste.messages;

import java.nio.ByteBuffer;

public class CsdRestrictedStatus extends RestrictedStatus{
	public static final int DATA_SECTION_SIZE = 51;
	
	
	
	public static ECoCRestrictedStatus fromBuffer(ByteBuffer b) {
		//ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH + EventLogMsg.EVENT_LOG_COMMON_HEADER, SECTION_SIZE);
		byte errorCode = b.get();
		byte []restrictedDataSection = new byte[DATA_SECTION_SIZE];
		b.get(restrictedDataSection);

		return null;
	}
	
	public CsdRestrictedStatus(
			byte errorCode,
			byte[] restrictedDataSection){
		this.errorCode = errorCode;
		this.restrictedDataSection = restrictedDataSection;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(DATA_SECTION_SIZE+1);

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
		// TODO Auto-generated method stub
		return DATA_SECTION_SIZE + 1;
	}
}
