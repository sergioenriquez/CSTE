package cste.icd.components;

import cste.icd.icd_messages.RestrictedStatus;
import cste.icd.icd_messages.RestrictedStatusECM;
import cste.icd.types.DeviceUID;

public class CSD extends ComModule{
	private static final long serialVersionUID = -5163798724291923073L;
	private RestrictedStatusECM status = null;
	
	public CSD(DeviceUID devUID, byte[] address) {
		super(devUID, address);
	}
	
	public RestrictedStatus getRestrictedStatus(){
		return status;
	}
	
	public void setRestrictedStatus(RestrictedStatus latestStatus){
		status = null;
	}

	@Override
	public int getAlarmCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getArmedStatus() {
		// TODO Auto-generated method stub
		return false;
	}
}
