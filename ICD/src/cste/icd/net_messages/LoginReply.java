package cste.icd.net_messages;

import cste.icd.general.IcdPayload;

public class LoginReply extends IcdPayload{
	public static final int SECTION_SIZE = 1;
	public byte replyCode;
	
	@Override
	public byte[] getBytes() {
		return new byte[]{replyCode};
	}

	@Override
	public String toString() {
		return String.valueOf(replyCode);
	}

	@Override
	public byte getSize() {
		return SECTION_SIZE;
	}

}
