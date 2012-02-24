package cste.messages;

import java.nio.ByteBuffer;

public class UnrestrictedCommand {
	public static final int SECTION_SIZE = 2;
	public byte commandCode;
	public byte checksum;
	
	public static UnrestrictedCommand fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,SECTION_SIZE);
		
		byte commandCode = b.get();
		byte checksum = b.get();

		return new UnrestrictedCommand(
				commandCode,
				checksum
		);
	}
	
	public UnrestrictedCommand(
			byte commandCode,
			byte checksum		
			){
		this.commandCode = commandCode;
		this.checksum = checksum;
	}
}
