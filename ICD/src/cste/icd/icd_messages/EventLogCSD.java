package cste.icd.icd_messages;

import java.nio.ByteBuffer;

import cste.icd.types.EcmEventLogType;
import cste.icd.types.IcdTimestamp;

public class EventLogCSD extends EventLogICD{
	public static final int SECTION_SIZE = 32;

	public byte operatingMode;
	public byte restrictedStatus;
	public byte restrictedError;
	public byte alarmStatus;
	public byte[] conveyanceID;
	public byte doorStatus;
	public byte cmdMsgType;
	public byte cmdOpCode;
	
	
	@Override
	public String getStatusStr(){
		String txt = "";
		return txt;
	}
	
	public EventLogCSD(IcdTimestamp timeStamp, byte eventType, byte[] eventData) {
		this.eventType = EcmEventLogType.fromValue(eventType);
		this.timeStamp = timeStamp;
		
		ByteBuffer b = ByteBuffer.wrap(eventData);
		operatingMode = b.get();
		restrictedStatus = b.get();
		restrictedError = b.get();
		alarmStatus = b.get();
		conveyanceID = new byte[16];
		b.get(conveyanceID);
		doorStatus = b.get();
		cmdMsgType = b.get();
		cmdOpCode = b.get();
	}

	public EventLogCSD(ByteBuffer b) {
		ackNo = b.get();
		eventType = EcmEventLogType.fromValue(b.get());
		timeStamp = new IcdTimestamp(b);
		
		operatingMode = b.get();
		restrictedStatus = b.get();
		restrictedError = b.get();
		alarmStatus = b.get();
		conveyanceID = new byte[16];
		b.get(conveyanceID);
		doorStatus = b.get();
		cmdMsgType = b.get();
		cmdOpCode = b.get();
	}

	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		b.put(ackNo);
		b.put(eventType.getBytes());
		b.put(timeStamp.getBytes());
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(alarmStatus);
		b.put(conveyanceID);
		b.put(doorStatus);
		b.put(cmdMsgType);
		b.put(cmdOpCode);
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
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(alarmStatus);
		b.put(conveyanceID);
		b.put(doorStatus);
		b.put(cmdMsgType);
		b.put(cmdOpCode);
		return b.array();
	}
}
