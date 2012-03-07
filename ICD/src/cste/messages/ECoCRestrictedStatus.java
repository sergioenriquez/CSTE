package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.TimeStamp;

public class ECoCRestrictedStatus extends RestrictedStatus{
	public static final int DATA_SECTION_SIZE = 66;
	
	public byte errorCode;
	
	public byte ackAscension;
	public byte opMode;
	public byte statusBits;
	public byte errorBits;
	public byte sensorErrorBits;
	public byte[] coveyanceID;
	public TimeStamp time;
	public byte[] gpsLoc; //TODO use class for gps loc;
	public byte alarmCode;
	
	public byte[] restrictedDataSection;

	
	public ECoCRestrictedStatus(ByteBuffer b){
		this.errorCode = b.get();
		this.ackAscension = b.get();
		this.opMode = b.get();
		this.statusBits = b.get();
		this.errorBits = b.get();
		this.sensorErrorBits = b.get();
		this.coveyanceID = new byte[16];
		b.get(coveyanceID);
		this.time = new TimeStamp(b);
		this.gpsLoc = new byte[16];
		b.get(gpsLoc);
		this.alarmCode = b.get();
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(DATA_SECTION_SIZE+1);

		b.put(errorCode);
		b.put(restrictedDataSection);

		return b.array();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return DATA_SECTION_SIZE + 1;
	}
}
