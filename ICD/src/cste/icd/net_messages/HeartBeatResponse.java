package cste.icd.net_messages;

import cste.icd.general.IcdPayload;
import cste.icd.types.IcdTimestamp;
import cste.icd.types.MaintenaceCode;

public class HeartBeatResponse extends IcdPayload{
	public MaintenaceCode maintenaceCode;
	public IcdTimestamp timestamp;
	
	public HeartBeatResponse(MaintenaceCode maintenaceCode, IcdTimestamp timestamp){
		this.maintenaceCode = maintenaceCode;
		this.timestamp = timestamp;
	}
	
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
