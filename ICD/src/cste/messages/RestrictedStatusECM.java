package cste.messages;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cste.icd.ConveyanceID;
import cste.icd.GpsLoc;
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
	public ConveyanceID coveyanceID;
	public IcdTimestamp time;
	public GpsLoc gpsLoc;
	public byte alarmCode;
	
	public byte[] restrictedDataSection;

	public RestrictedStatusECM(ByteBuffer b){
		this.errorCode = b.get();
		this.ackNo = b.get();
		this.opMode = b.get();
		this.statusBits = b.get();
		this.errorBits = b.get();
		this.sensorErrorBits = b.get();
		this.coveyanceID = new ConveyanceID(b);
		this.time = new IcdTimestamp(b);
		this.gpsLoc = new GpsLoc(b);
		this.alarmCode = b.get();
	}

	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);

		//TODO

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
