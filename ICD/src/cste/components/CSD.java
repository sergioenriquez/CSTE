package cste.components;

import cste.icd.DeviceUID;
import cste.messages.RestrictedStatus;
import cste.messages.RestrictedStatusECM;

public class CSD extends ComModule{
	private static final long serialVersionUID = -5163798724291923073L;
	private RestrictedStatusECM status = null;
	
	public CSD(DeviceUID devUID) {
		super(devUID);
		// TODO Auto-generated constructor stub
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
