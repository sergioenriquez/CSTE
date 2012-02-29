package cste.messages;

import java.nio.ByteBuffer;

public class CsdLogDeviceStatus extends IcdPayload{
	
	public static final int CSD_STATUS_SIZE = 51;
	
	public final byte operatingMode;
	public final byte restrictedStatus;
	public final byte alarmStatus;
	public final byte[] conveyanceID;
	public final byte doorStatus;
	public final byte cmdMsgType;
	public final byte cmdOpCode;

	public static CsdLogDeviceStatus fromBuffer(ByteBuffer b) {
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
	
	@Override
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
