package cste.misc;

import cste.icd.icd_messages.IcdMsg;
import cste.icd.types.IcdTimestamp;
import cste.icd.types.NadEventLogType;

public class HnadEventLog {
	public int logID;
	public String username;
	public IcdTimestamp timeStamp;
	public NadEventLogType eventType;
	public IcdMsg msgSent;
	public IcdMsg msgReceived;
	
	public HnadEventLog(
			int logID,
			String username,
			IcdTimestamp timeStamp,
			NadEventLogType eventType,
			IcdMsg msgSent,
			IcdMsg msgReceived){
		this.logID = logID;
		this.username = username;
		this.timeStamp = timeStamp;
		this.eventType = eventType;
		this.msgSent = msgSent;
		this.msgReceived = msgReceived;
	}
}
