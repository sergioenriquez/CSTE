package cste.icd.icd_messages;


import java.nio.ByteBuffer;

import cste.icd.types.EcmEventLogType;
import cste.icd.types.IcdTimestamp;

public class EventLogECM extends EventLogICD{
	public static final int SECTION_SIZE = 63;
	
	public byte reserved;
	public byte operatingMode;
	public byte restrictedStatus;
	public byte restrictedError;
	public byte[] conveyanceID;
	public byte[] gpsLocation;
	public byte lockStatus;
	
	@Override
	public String getStatusStr(){
		StringBuilder str = new StringBuilder();
		//str.append("Res "); str.append(reserved); str.append(", ");
		str.append("OpM "); str.append(operatingMode);str.append(", ");
		str.append("Sta "); str.append(restrictedStatus);str.append(", ");
		str.append("Err "); str.append(restrictedError);str.append(", ");
		str.append("Lck "); str.append(lockStatus);
		return str.toString();
	}
	//outdated
	public EventLogECM(IcdTimestamp timeStamp, byte eventType, byte[] eventData) {
		this.eventType = EcmEventLogType.fromValue(eventType);
		this.timeStamp = timeStamp;
		ByteBuffer b = ByteBuffer.wrap(eventData);
		reserved = b.get();
		operatingMode = b.get();
		restrictedStatus = b.get();
		restrictedError = b.get();
		conveyanceID = new byte[16];
		b.get(conveyanceID);
		gpsLocation = new byte[16];
		b.get(gpsLocation);
		lockStatus = b.get();
	}

	public EventLogECM(ByteBuffer b) {
		ackNo = b.get();
		logRecordNum = b.getShort();
		eventType = EcmEventLogType.fromValue(b.get());
		timeStamp = new IcdTimestamp(b);
		//status section
		reserved = b.get();
		operatingMode = b.get();
		restrictedStatus = b.get();
		restrictedError = b.get();
		conveyanceID = new byte[16];
		b.get(conveyanceID);
		gpsLocation = new byte[20];
		b.get(gpsLocation);
		lockStatus = b.get();
	}
	

	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(ackNo);
		b.putShort(logRecordNum);
		b.put(eventType.getBytes());
		b.put(timeStamp.getBytes());
		b.put(reserved);
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(conveyanceID);
		b.put(gpsLocation);
		b.put(lockStatus);
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

	@Override
	public byte[] getStatusSection() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(reserved);
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(conveyanceID);
		b.put(gpsLocation);
		b.put(lockStatus);
		return b.array();
	}
}
