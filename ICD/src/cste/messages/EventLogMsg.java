package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.DeviceType;

public class EventLogMsg {
	public static final int EVENT_LOG_COMMON_HEADER = 10;
	
	public byte ackNo;
	public byte eventType;
	public long time;
	
	public Object statusData;
	private DeviceType type;
	
	
	//TODO add accessor functions
	
	public static EventLogMsg fromBytes(DeviceType type,byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,EVENT_LOG_COMMON_HEADER);
		
		byte ackNo = b.get();
		byte eventType = b.get();
		long time = b.getLong();
		Object statusData = null;
		
		if ( type == DeviceType.CSD)
			statusData = CsdLogDeviceStatus.fromBytes(content);
		else if ( type == DeviceType.ECOC)
			statusData = EcocLogDeviceStatus.fromBytes(content);
		else
			return null;

		return new EventLogMsg(
				type,
				ackNo,
				eventType,
				time,
				statusData
		);
	}
	
	public EventLogMsg(
			DeviceType type,
			byte ackNo,
			byte eventType,
			long time,
			Object statusData			
			){
		this.type = type;
		this.ackNo = ackNo;
		this.eventType = eventType;
		this.time = time;
		this.statusData = statusData;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = null;
		if ( type == DeviceType.CSD){
			b = ByteBuffer.allocate(EVENT_LOG_COMMON_HEADER + CsdLogDeviceStatus.CSD_STATUS_SIZE);
			b.put(ackNo);
			b.put(eventType);
			b.putLong(time);
			b.put( ((CsdLogDeviceStatus)statusData).getBytes() );
		}
		else if ( type == DeviceType.CSD){
			b = ByteBuffer.allocate(EVENT_LOG_COMMON_HEADER + EcocLogDeviceStatus.ECOC_STATUS_SIZE);
			b.put(ackNo);
			b.put(eventType);
			b.putLong(time);
			b.put( ((EcocLogDeviceStatus)statusData).getBytes() );
		}

		return b.array();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}
}
