package cste.messages;

import java.nio.ByteBuffer;

import cste.icd.EcocCmdType;
import cste.icd.UnrestrictedCmdType;

public class UnrestrictedCommand extends IcdPayload {
	public static final int SECTION_SIZE = 2;
	public byte commandCode;
	byte checksum;
	
	public static UnrestrictedCommand fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,SECTION_SIZE);
		byte commandCode = b.get();
		byte checksum = b.get();
		return new UnrestrictedCommand(commandCode,checksum);
	}

	private UnrestrictedCommand( byte type, byte checksum ){
		this.commandCode = type;
		this.checksum = checksum;
	}
	
	public UnrestrictedCommand(	UnrestrictedCmdType type ){
		this.commandCode = type.getBytes();
		this.checksum = 0;
	}

	@Override
	public byte[] getBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.put(commandCode);
		buffer.put(checksum);
		return buffer.array();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	
	public void putChecksum(IcdHeader header) {
		checksum = 0;
		for(byte b : header.getBytes())
			checksum += b;
		checksum += commandCode;
	}
}
