package cste.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cste.icd.IcdTimestamp;

public class RestrictedStatusECM extends RestrictedStatus{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6297952462704210321L;

	public static final int SECTION_SIZE = 67;
	
	//ackNo
	public byte opMode;
	public byte statusBits;
	public byte errorBits;
	public byte sensorErrorBits;
	public byte[] coveyanceID;
	public IcdTimestamp time;
	public byte[] gpsLoc; //TODO use class for gps loc;
	public byte alarmCode;
	
	public byte[] restrictedDataSection;

	public RestrictedStatusECM(ByteBuffer b){
		this.errorCode = b.get();
		this.ackNo = b.get();
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
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);

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
		return SECTION_SIZE;
	}
}
