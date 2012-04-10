package cste.icd.net_messages;

import static cste.icd.general.Constants.ICD_REV_NUMBER;
import cste.icd.general.IcdPayload;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.types.DeviceUID;
import cste.icd.types.MaintenaceCode;

public class NetPkt {
	public Preamble preamble;
	public IcdPayload payload;
	public static DeviceUID ThisUID;
	public boolean replyExpected;
	
	public byte[] getBytes(){
		return null;
	}
	
	public static NetPkt fromByteArray(byte []buffer){
		//TODO implement
		//ByteBuffer b = ByteBuffer.wrap(buffer);
		Preamble preamble = null;
		IcdPayload payload = null;
		
		return new NetPkt(preamble,payload,false);
	}
	
	public static NetPkt buildHeartbeatMsg(MaintenaceCode code){
		Preamble preamble = new Preamble(
				ICD_REV_NUMBER,
				FunctionCode.HEARTBEAT_REQUEST,
				ThisUID);
		IcdPayload payload = new HeartBeat(code);
		return new NetPkt(preamble,payload,true);
	}
	
	public static NetPkt buildIcdtMsg(IcdMsg msg){
		Preamble preamble = new Preamble(
				ICD_REV_NUMBER,
				FunctionCode.HEARTBEAT_REQUEST,
				ThisUID);
		IcdPayload payload = msg.payload;
		return new NetPkt(preamble,payload,false);
	}
	
	protected NetPkt(Preamble preamble, IcdPayload payload , boolean replyExpected){
		this.preamble = preamble;
		this.payload = payload;
		this.replyExpected = replyExpected;
	}
}
