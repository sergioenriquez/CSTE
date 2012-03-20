package cste.notused;

import java.nio.ByteBuffer;

import cste.icd.general.IcdPayload;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.net_messages.FunctionCode;
import cste.icd.net_messages.HeartBeat;
import cste.icd.net_messages.LoginRequest;
import cste.icd.net_messages.Preamble;
import cste.icd.types.DeviceUID;
import cste.icd.types.MaintenaceCode;
import static cste.icd.general.Constants.*;

public class NetPkt {
	public Preamble preamble;
	public IcdPayload payload;
	public static DeviceUID ThisUID;
	public boolean replyExpected;
	
	public byte[] getBytes(){
		return null;
	}
	
	public static NetPkt fromByteArray(byte []buffer){
		ByteBuffer b = ByteBuffer.wrap(buffer);
		Preamble preamble = null;
		IcdPayload payload = null;
		
		return new NetPkt(preamble,payload,false);
	}
	
	public static NetPkt buildLoginRequestMsg(String username,String password){
		Preamble preamble = new Preamble(
				ICD_REV_NUMBER,
				FunctionCode.LOGIN_REQUEST,
				ThisUID);
		IcdPayload payload = new LoginRequest(username,password);
		return new NetPkt(preamble,payload,true);
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
