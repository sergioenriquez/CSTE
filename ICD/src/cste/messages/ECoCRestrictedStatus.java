package cste.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cste.icd.IcdTimestamp;

public class ECoCRestrictedStatus extends RestrictedStatus{
	public static final int DATA_SECTION_SIZE = 66;
	
	public byte errorCode;
	
	public byte ackAscension;
	public byte opMode;
	public byte statusBits;
	public byte errorBits;
	public byte sensorErrorBits;
	public byte[] coveyanceID;
	public IcdTimestamp time;
	public byte[] gpsLoc; //TODO use class for gps loc;
	public byte alarmCode;
	
	public byte[] restrictedDataSection;
	
	public String getConveyanceStr(){
		String conveyanceName;
		try {
			conveyanceName = new String(coveyanceID, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			conveyanceName = "encoding error";
		}
		return conveyanceName;
	}
	
	public String getGpsStr(){
		String gpsStr;
		try {
			gpsStr = new String(gpsLoc, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			gpsStr = "encoding error";
		}
		return gpsStr;
	}

	public ECoCRestrictedStatus(ByteBuffer b){
		this.errorCode = b.get();
		this.ackAscension = b.get();
		this.opMode = b.get();
		this.statusBits = b.get();
		this.errorBits = b.get();
		this.sensorErrorBits = b.get();
		this.coveyanceID = new byte[16];
		b.get(coveyanceID);
		this.time = new IcdTimestamp(b);
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
