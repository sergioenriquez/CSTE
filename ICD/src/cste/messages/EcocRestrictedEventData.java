package cste.messages;

import java.nio.ByteBuffer;

public class EcocRestrictedEventData {
	public static final int SECTION_SIZE = 51;
	
	public byte ackAscNum;
	public byte operatingMode;
	public byte restrictedStatusCode;
	public byte restrictedErrorCode;
	public byte sensorErrorCode;
	public byte[] conveyanceID;
	public byte[] utcTime;
	public byte[] gpsLoc;
	public byte masterAlarm;
	public byte doorStatus;
	public byte[] reserved;
	
	
	public static EcocRestrictedEventData fromBuffer(ByteBuffer b) {
		//ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH + EventLogMsg.EVENT_LOG_COMMON_HEADER, SECTION_SIZE);
		byte ackAscNum = b.get();
		byte operatingMode = b.get();
		byte sensorMode = b.get();
		byte restrictedStatusCode = b.get();
		byte sensorErrorCode = b.get();
		byte[] conveyanceID = new byte[16];
		b.get(conveyanceID, 0, 16);
		byte[] utcTime = new byte[8];
		b.get(utcTime, 0, 16);
		byte[] gpsLoc = new byte[20];
		b.get(gpsLoc, 0, 20);
		byte masterAlarm = b.get();
		byte doorStatus = b.get();
		byte[] reserved = new byte[16];
		b.get(reserved, 0, 16);

		return new EcocRestrictedEventData(
				ackAscNum,
				operatingMode,
				sensorMode,
				restrictedStatusCode,
				sensorErrorCode,
				conveyanceID,
				utcTime,
				gpsLoc,
				masterAlarm,
				doorStatus,
				reserved);
	}
	
	public EcocRestrictedEventData(
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
		this.restrictedErrorCode = sensorMode;
		this.restrictedStatusCode = restrictedStatusCode;
		this.sensorErrorCode = sensorErrorCode;
		this.utcTime = mechSealID;
		this.conveyanceID = conveyanceID;
		this.gpsLoc = manifestID;
		this.masterAlarm = alarmStatus;
		this.doorStatus = doorStatus;
		this.reserved = reserved;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);

		b.put(ackAscNum);
		b.put(operatingMode);
		b.put(restrictedStatusCode);
		b.put(restrictedErrorCode);
		b.put(sensorErrorCode);
		b.put(conveyanceID);
		b.put(utcTime);
		b.put(gpsLoc);
		b.put(masterAlarm);
		b.put(doorStatus);
		b.put(reserved);
	
		return b.array();
	}
}
