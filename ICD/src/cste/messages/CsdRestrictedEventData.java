package cste.messages;

import java.nio.ByteBuffer;

public class CsdRestrictedEventData {
	public static final int SECTION_SIZE = 55;
	
	public byte ackAscNum;
	public byte operatingMode;
	public byte sensorMode;
	public byte restrictedStatusCode;
	public byte sensorErrorCode;
	public byte[] mechSealID;
	public byte[] conveyanceID;
	public byte[] manifestID;
	public byte alarmStatus;
	public byte doorStatus;
	public byte[] reserved;

	public static CsdRestrictedEventData fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH + EventLogMsg.EVENT_LOG_COMMON_HEADER, SECTION_SIZE);
		byte ackAscNum = b.get();
		byte operatingMode = b.get();
		byte sensorMode = b.get();
		byte restrictedStatusCode = b.get();
		byte sensorErrorCode = b.get();
		
		byte[] mechSealID = new byte[15];
		b.get(mechSealID, 0, 16);
		byte[] conveyanceID = new byte[16];
		b.get(conveyanceID, 0, 16);
		byte[] manifestID = new byte[16];
		b.get(manifestID, 0, 16);
		
		
		byte alarmStatus = b.get();
		byte doorStatus = b.get();
		
		byte[] reserved = new byte[16];
		b.get(reserved, 0, 16);

		return new CsdRestrictedEventData(
				ackAscNum,
				operatingMode,
				sensorMode,
				restrictedStatusCode,
				sensorErrorCode,
				mechSealID,
				conveyanceID,
				manifestID,
				alarmStatus,
				doorStatus,
				reserved);
	}
	
	public CsdRestrictedEventData(
			byte ackAscNum,
			byte operatingMode,
			byte sensorMode,
			byte restrictedStatusCode,
			byte sensorErrorCode,
			byte[] mechSealID,
			byte[] conveyanceID,
			byte[] manifestID,
			byte alarmStatus,
			byte doorStatus,
			byte[] reserved
			){
		this.ackAscNum = ackAscNum;
		this.operatingMode = operatingMode;
		this.sensorMode = sensorMode;
		this.restrictedStatusCode = restrictedStatusCode;
		this.sensorErrorCode = sensorErrorCode;
		this.mechSealID = mechSealID;
		this.conveyanceID = conveyanceID;
		this.manifestID = manifestID;
		this.alarmStatus = alarmStatus;
		this.doorStatus = doorStatus;
		this.reserved = reserved;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);

		b.put(ackAscNum);
		b.put(operatingMode);
		b.put(sensorMode);
		b.put(restrictedStatusCode);
		b.put(sensorErrorCode);
		b.put(mechSealID);
		b.put(conveyanceID);
		b.put(manifestID);
		b.put(alarmStatus);
		b.put(doorStatus);
		b.put(reserved);
	
		return b.array();
	}
}
