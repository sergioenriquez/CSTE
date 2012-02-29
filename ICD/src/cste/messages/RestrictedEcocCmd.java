package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.EcocCmdType;
import static cste.icd.Utility.getEncryptedSize;

public class RestrictedEcocCmd extends IcdPayload{
	public static final int SECTION_SIZE = 1;
	
	private final EcocCmdType cmdCode;
	protected final Object []params;
	
	@SuppressWarnings("unused")
	public static RestrictedEcocCmd create(Object ... params) {
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

		return new RestrictedEcocCmd(type, cmdParam);
	}
	
	@SuppressWarnings("unused")
	public static RestrictedEcocCmd fromBuffer(ByteBuffer b) {
		Object []cmdParam = new Object[4];
		for(Object p: cmdParam)
			p = null;
		
		byte val = b.get();
		EcocCmdType cmd = EcocCmdType.fromValue(val);
		switch(cmd){
		case NOP:
			break;
			//TODO read values as needed
		default:
		}

		return new RestrictedEcocCmd(cmd, cmdParam);
	}
	
	protected RestrictedEcocCmd(EcocCmdType cmdCode,Object []params){
		this.params = params;
		this.cmdCode = cmdCode;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);
		//TODO
		b.put(cmdCode.getBytes());
		
		switch(cmdCode){
		case NOP:
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
