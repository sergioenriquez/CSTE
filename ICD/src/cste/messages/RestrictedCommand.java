package cste.messages;

import java.nio.ByteBuffer;

public class RestrictedCommand {
	public static final int SECTION_SIZE = 2;
	public byte commandCode;
	
	public static RestrictedCommand fromBytes(byte[] content) {
		ByteBuffer b = ByteBuffer.wrap(content,IcdHeader.ICD_HEADER_LENGTH,SECTION_SIZE);
		
		byte commandCode = b.get();

		return new RestrictedCommand(
				commandCode
		);
	}
	
	public RestrictedCommand(
			byte commandCode
			){
		this.commandCode = commandCode;
	}
	
	public byte[] getBytes() {
		ByteBuffer b = ByteBuffer.allocate(SECTION_SIZE);

		b.put(commandCode);

		return b.array();
	}
}
