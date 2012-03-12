package cste.messages;

import cste.icd.EventLogType;
import cste.icd.IcdTimestamp;

public abstract class EventLogICD extends IcdPayload{
	public byte ackNo;
	public short logRecordNum;
	public EventLogType eventType;
	public IcdTimestamp timeStamp;
	
	public abstract byte[] getStatusSection();
	
	@Override
	public abstract byte[] getBytes();

	@Override
	public abstract String toString();

	@Override
	public abstract byte getSize();
	
	public abstract String getStatusStr();
}
