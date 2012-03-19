package cste.icd.icd_messages;

import cste.icd.general.IcdPayload;
import cste.icd.types.EcmEventLogType;
import cste.icd.types.IcdTimestamp;

public abstract class EventLogICD extends IcdPayload{
	public byte ackNo;
	public short logRecordNum;
	public EcmEventLogType eventType;
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
