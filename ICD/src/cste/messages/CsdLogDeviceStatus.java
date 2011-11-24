package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;

public class CsdLogDeviceStatus {
	
	public static final int CSD_STATUS_SIZE = 51;
	
	public byte operatingMode;
	public byte restrictedStatus;
	public byte alarmStatus;
	public byte[] conveyanceID;
	public byte doorStatus;
	public byte cmdMsgType;
	public byte cmdOpCode;

	public static CsdLogDeviceStatus fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH + EventLogMsg.EVENT_LOG_COMMON_HEADER, CSD_STATUS_SIZE);
		byte operatingMode = b.get();
		byte restrictedStatus = b.get();
		byte alarmStatus = b.get();
		byte[] conveyanceID = new byte[16];
		b.get(conveyanceID, 0, 16);
		byte doorStatus = b.get();
		byte cmdMsgType = b.get();
		byte cmdOpCode = b.get();
		
		return new CsdLogDeviceStatus(
				operatingMode,
				restrictedStatus,
				alarmStatus,
				conveyanceID,
				doorStatus,
				cmdMsgType,
				cmdOpCode);
	}
	
	public CsdLogDeviceStatus(
			byte operatingMode,
			byte restrictedStatus,
			byte alarmStatus,
			byte[] conveyanceID,
			byte doorStatus,
			byte cmdMsgType,
			byte cmdOpCode){
		this.operatingMode = operatingMode;
		this.restrictedStatus = restrictedStatus;
		this.alarmStatus = alarmStatus;
		this.conveyanceID = conveyanceID;
		this.doorStatus = doorStatus;
		this.cmdMsgType = cmdMsgType;
		this.cmdOpCode = cmdOpCode;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(CSD_STATUS_SIZE);
		b.put(operatingMode);
		b.put(restrictedStatus);
		b.put(alarmStatus);
		b.put(conveyanceID);
		b.put(doorStatus);
		b.put(cmdMsgType);
		b.put(cmdOpCode);
		return b.array();
	}
}
