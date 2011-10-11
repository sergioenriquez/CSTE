package cste.messages;

import java.nio.ByteBuffer;
import java.util.Date;

public class CsdEventLogMsg {
	public static final int CSD_EVENT_LOG_MSG_SIZE = 48;
	public byte ackNo;
	public byte eventType;
	public long time;
	public byte operatingMode;
	public byte restrictedStatus;
	public byte alarmStatus;
	public byte[] conveyanceID;
	public byte doorStatus;
	public byte cmdMsgType;
	public byte cmdOpCode;
	
	//TODO add accessor functions
	
	public static CsdEventLogMsg fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,CSD_EVENT_LOG_MSG_SIZE);
		byte ackNo = b.get();
		byte eventType = b.get();
		long time = b.getLong();
		byte operatingMode = b.get();
		byte restrictedStatus = b.get();
		byte alarmStatus = b.get();
		byte[] conveyanceID = new byte[16];
		b.get(conveyanceID, 0, 16);
		byte doorStatus = b.get();
		byte cmdMsgType = b.get();
		byte cmdOpCode = b.get();
		
		return new CsdEventLogMsg(
				ackNo,
				eventType,
				time,
				operatingMode,
				restrictedStatus,
				alarmStatus,
				conveyanceID,
				doorStatus,
				cmdMsgType,
				cmdOpCode
		);
	}
	
	public CsdEventLogMsg(
			byte ackNo,
			byte eventType,
			long time,
			byte operatingMode,
			byte restrictedStatus,
			byte alarmStatus,
			byte[] conveyanceID,
			byte doorStatus,
			byte cmdMsgType,
			byte cmdOpCode			
			){
		this.ackNo = ackNo;
		this.eventType = eventType;
		this.time = time;
		this.operatingMode = operatingMode;
		this.restrictedStatus = restrictedStatus;
		this.alarmStatus = alarmStatus;
		this.conveyanceID = conveyanceID;
		this.doorStatus = doorStatus;
		this.cmdMsgType = cmdMsgType;
		this.cmdOpCode = cmdOpCode;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(CSD_EVENT_LOG_MSG_SIZE);
		b.put(ackNo);
		b.put(eventType);
		b.putLong(time);
		b.put(operatingMode);
		b.put(restrictedStatus);
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
}
