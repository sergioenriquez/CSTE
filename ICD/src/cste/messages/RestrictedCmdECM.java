package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.EcocCmdType;
import cste.icd.IcdTimestamp;
import static cste.icd.Utility.getEncryptedSize;

public class RestrictedCmdECM extends IcdPayload{
	public static final int SECTION_SIZE = 1;
	
	private final EcocCmdType cmdCode;
	public final Object []params;
	
	@SuppressWarnings("unused")
	public static RestrictedCmdECM create(Object ... params) {
		if( params.length == 0)
			return null; //TODO LOG
		
		EcocCmdType type = (EcocCmdType)params[0];
		if( type.getParamCnt() != params.length-1)
			return null; //TODO LOG
		
		Object []cmdParam = new Object[4];
		for(Object p: cmdParam)
			p = null;
		
		for(int i=0; i < type.getParamCnt() ;i++)
			cmdParam[i] = params[i+1];

		return new RestrictedCmdECM(type, cmdParam);
	}
	
	@SuppressWarnings("unused")
	public static RestrictedCmdECM fromBuffer(ByteBuffer b) {
		Object []cmdParam = new Object[4];
		for(Object p: cmdParam)
			p = null;
		
		byte val = b.get();
		EcocCmdType cmd = EcocCmdType.fromValue(val);
		switch(cmd){
		case ACK:
			cmdParam[0] = b.get();
			break;
		case NOP:
			break;
			//TODO read other values as needed
		default:
		}

		return new RestrictedCmdECM(cmd, cmdParam);
	}
	
	protected RestrictedCmdECM(EcocCmdType cmdCode,Object []params){
		this.params = params;
		this.cmdCode = cmdCode;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(cmdCode.getSize());
		//TODO
		b.put(cmdCode.getBytes());
		
		switch(cmdCode){
		case NOP:
		case SMAT:
		case SMAF:
		case DAHH:
		case DADC:
		case SL:
		case EL:
			//No parameters
			break;
		case ACK:
			Byte ackNo = (Byte)params[0];
			b.put(ackNo);
			break;
		case ST:
			IcdTimestamp t = (IcdTimestamp)params[0];
			b.put(t.getBytes());
			break;
		case CWT:
			//1 param
			break;
		case CPI:
			//1 param
			break;
		case WLN:
			//4 params
			break;
		case WLA:
			//4 params
			break;
			//TODO read write values as needed
		default:
		}

		return b.array();
	}

	@Override
	public String toString() {
		return cmdCode.toString();
	}

	@Override
	public byte getSize() {
		return (byte) getEncryptedSize(cmdCode.getSize());
	}
}
