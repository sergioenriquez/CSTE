package cste.misc;

import java.io.Serializable;

import cste.icd.icd_messages.IcdMsg;
import cste.icd.types.IcdTimestamp;
import cste.icd.types.NadEventLogType;

public class HnadEventLog implements Serializable{
	private static final long serialVersionUID = 1L;
	
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
