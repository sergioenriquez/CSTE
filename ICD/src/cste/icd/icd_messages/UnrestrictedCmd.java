package cste.icd.icd_messages;

import java.nio.ByteBuffer;

import cste.icd.general.IcdPayload;
import cste.icd.types.IcdHeader;
import cste.icd.types.UnrestrictedCmdType;

public class UnrestrictedCmd extends IcdPayload {
	public static final int SECTION_SIZE = 2;
	public byte commandCode;
	byte checksum;
	
	public static UnrestrictedCmd fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.SECTION_SIZE,SECTION_SIZE);
		byte commandCode = b.get();
		byte checksum = b.get();
		return new UnrestrictedCmd(commandCode,checksum);
	}

	private UnrestrictedCmd( byte type, byte checksum ){
		this.commandCode = type;
		this.checksum = checksum;
	}
	
	public UnrestrictedCmd(	UnrestrictedCmdType type ){
		this.commandCode = type.getBytes();
		this.checksum = 0;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(commandCode);
		//buffer.put(checksum);
		return buffer.array();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		return 2;
	}
	
	
	public void putChecksum(IcdHeader header) {
		checksum = 0;
		for(byte b : header.getBytes())
			checksum += b;
		checksum += commandCode;
	}
}