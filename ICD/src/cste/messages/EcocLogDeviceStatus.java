package cste.messages;

import java.nio.ByteBuffer;

public class EcocLogDeviceStatus {
	public static final int ECOC_STATUS_SIZE = 51;
	
	public byte ackAscNum;
	public byte operatingMode;
	public byte restrictedStatus;
	public byte restrictedError;
	public byte[] conveyanceID;
	public byte[] gpsLocation;
	public byte lockStatus;

	public static EcocLogDeviceStatus fromBuffer(ByteBuffer b) {
		//ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH + EventLogMsg.EVENT_LOG_COMMON_HEADER, ECOC_STATUS_SIZE);
		byte ackAscNum = b.get();
		byte operatingMode = b.get();
		byte restrictedStatus = b.get();
		byte restrictedError = b.get();
		byte[] conveyanceID = new byte[16];
		b.get(conveyanceID, 0, 16);
		byte[] gpsLocation = new byte[16];
		b.get(gpsLocation, 0, 16);
		byte lockStatus = b.get();

		return new EcocLogDeviceStatus(
				ackAscNum,
				operatingMode,
				restrictedStatus,
				restrictedError,
				conveyanceID,
				gpsLocation,
				lockStatus);
	}
	
	public EcocLogDeviceStatus(
			byte ackAscNum,
			byte operatingMode,
			byte restrictedStatus,
			byte restrictedError,
			byte[] conveyanceID,
			byte[] gpsLocation,
			byte lockStatus){
		this.ackAscNum = ackAscNum;
		this.operatingMode = operatingMode;
		this.restrictedStatus = restrictedStatus;
		this.restrictedError = restrictedError;
		this.conveyanceID = conveyanceID;
		this.gpsLocation = gpsLocation;
		this.lockStatus = lockStatus;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(ECOC_STATUS_SIZE);

		b.put(ackAscNum);
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(restrictedError);
		b.put(conveyanceID);
		b.put(gpsLocation);
		b.put(lockStatus);
	
		return b.array();
	}
}
