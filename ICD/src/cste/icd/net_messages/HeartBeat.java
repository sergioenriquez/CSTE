package cste.icd.net_messages;

import cste.icd.general.IcdPayload;
import cste.icd.types.IcdTimestamp;

public class HeartBeat extends IcdPayload{
	public byte maintenaceCode;
	public IcdTimestamp timestamp;
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
