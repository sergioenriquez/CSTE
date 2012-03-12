package cste.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cste.icd.EventLogType;
import cste.icd.IcdTimestamp;

public class EventLogECM extends EventLogICD{
	public static final int SECTION_SIZE = 61;
	
	public byte ackAscNum;
	public byte operatingMode;
	public byte restrictedStatus;
	public byte restrictedError;
	public byte[] conveyanceID;
	public byte[] gpsLocation;
	public byte lockStatus;
	
	@Override
	public String getStatusStr(){
		StringBuilder str = new StringBuilder();
		str.append(ackAscNum); str.append(',');
		str.append(operatingMode);str.append(',');
		str.append(restrictedStatus);str.append(',');
		str.append(restrictedError);str.append(',');
		
//		String gpsStr;
//		String conveyanceStr;
//		try {
//			gpsStr = new String(gpsLocation, "US-ASCII");
//			conveyanceStr = new String(conveyanceID, "US-ASCII");
//		} catch (UnsupportedEncodingException e) {
//			gpsStr = "encoding error";
//			conveyanceStr = "encoding error";
//		}
//		
//		str.append(gpsStr);str.append(',');
//		str.append(conveyanceStr);str.append(',');
		
		str.append(lockStatus);
		return str.toString();
	}
	
	public EventLogECM(IcdTimestamp timeStamp, byte eventType, byte[] eventData) {
		this.eventType = EventLogType.fromValue(eventType);
		this.timeStamp = timeStamp;
		ByteBuffer b = ByteBuffer.wrap(eventData);
		ackAscNum = b.get();
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
		eventType = EventLogType.fromValue(b.get());
		timeStamp = new IcdTimestamp(b);
		ackAscNum = b.get();
		operatingMode = b.get();
		restrictedStatus = b.get();
		restrictedError = b.get();
		conveyanceID = new byte[16];
		b.get(conveyanceID);
		gpsLocation = new byte[16];
		b.get(gpsLocation);
		lockStatus = b.get();
	}

	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(ackNo);
		b.put(eventType.getBytes());
		b.put(timeStamp.getBytes());
		b.put(ackAscNum);
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
		b.put(ackAscNum);
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(conveyanceID);
		b.put(gpsLocation);
		b.put(lockStatus);
		return b.array();
	}
}
