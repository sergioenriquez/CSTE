package cste.messages;

import java.nio.ByteBuffer;
import java.util.List;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class NADA {
	final int NADA_MIN_SIZE = 31;
	final DeviceType devType;
	final MsgType msgType = MsgType.NADA_MSG;
	final byte timeDelayCode;
	final TimeStamp timestamp;
	final DeviceType lvl1DevType;
	final DeviceUID lvl1UID;
	final DeviceType lvl2DevType;
	final DeviceUID lvl2UID;
	final List<DeviceUID> msgWaitingList;
	
	public NADA(
		DeviceType devType,
		int delayCode,
		DeviceType lvl1DevType,
		DeviceUID lvl1UID,
		DeviceType lvl2DevType,
		DeviceUID lvl2UID,
		List<DeviceUID> msgWaitingList){
		
		this.devType = devType;
		//this.timeDelayCode = (byte)(0x10); // 
		this.timeDelayCode = (byte)(0x01 | delayCode<<4 );
		this.timestamp = TimeStamp.now();	
		this.lvl1DevType = lvl1DevType;
		this.lvl1UID = lvl1UID;
		this.lvl2DevType = lvl2DevType;
		this.lvl2UID = lvl2UID;
		this.msgWaitingList = msgWaitingList;
	}
	
	public byte[] getBytes()
	{
		ByteBuffer temp = ByteBuffer.allocate(NADA_MIN_SIZE + msgWaitingList.size()*DeviceUID.SIZE);
		temp.put(devType.getBytes());
		temp.put(msgType.getBytes());
		temp.put(timeDelayCode);
		temp.put(timestamp.getBytes());
		temp.put(lvl1DevType.getBytes());
		temp.put(lvl1UID.getBytes());
		temp.put(lvl2DevType.getBytes());
		temp.put(lvl2UID.getBytes());
		
		temp.put((byte)msgWaitingList.size());
		for (DeviceUID uid: msgWaitingList )
			temp.put(uid.getBytes());

		byte checksum=0;
		for(byte b: temp.array())
			checksum += b;
		temp.put(checksum);
		
		return temp.array();
	}
}//end