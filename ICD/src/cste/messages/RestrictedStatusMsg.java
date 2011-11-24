package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;

public class RestrictedStatusMsg {
	public static final int RESTRICTED_EVENT_COMMON_HEADER = 1;
	public byte errorCode;
	public Object dataSection;
	private DeviceType type;
	
	public static RestrictedStatusMsg fromBytes(DeviceType type,byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,RESTRICTED_EVENT_COMMON_HEADER);
		
		byte errorCode = b.get();
		Object dataSection = null;
		
		if ( type == DeviceType.CSD)
			dataSection = CsdRestrictedEventData.fromBytes(content);
		else if ( type == DeviceType.ECOC)
			dataSection = EcocRestrictedEventData.fromBytes(content);
		else
			return null;

		return new RestrictedStatusMsg(
				type,
				errorCode,
				dataSection
		);
	}

	public RestrictedStatusMsg(
			DeviceType type,
			byte errorCode,
			Object dataSection			
			){
		this.type = type;
		this.errorCode = errorCode;
		this.dataSection = dataSection;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = null;
		if ( type == DeviceType.CSD){
			b = ByteBuffer.allocate(RESTRICTED_EVENT_COMMON_HEADER + CsdRestrictedEventData.SECTION_SIZE);
			b.put(errorCode);
			b.put( ((CsdRestrictedEventData)dataSection).getBytes() );
		}
		else if ( type == DeviceType.CSD){
			b = ByteBuffer.allocate(RESTRICTED_EVENT_COMMON_HEADER + EcocRestrictedEventData.SECTION_SIZE);
			b.put(errorCode);
			b.put( ((EcocRestrictedEventData)dataSection).getBytes() );
		}

		return b.array();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}
}
