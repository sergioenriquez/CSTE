package cste.icd.net_messages;

import cste.icd.general.IcdPayload;

public class KeyReply extends IcdPayload{
	public int SECTION_SIZE = 1;
	public byte keyCount;
	public byte []keys;
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
