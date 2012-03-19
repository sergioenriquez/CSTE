package cste.icd.components;

import cste.icd.icd_messages.RestrictedStatus;
import cste.icd.icd_messages.RestrictedStatusECM;
import cste.icd.types.ConveyanceID;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import cste.icd.types.GpsLoc;

public class ECoC extends ComModule{
	private static final long serialVersionUID = -5558388937219786697L;
	private RestrictedStatusECM status = null;
	
	public ECoC(DeviceUID devUID , byte[] address) {
		super(devUID,address);
		this.devType = DeviceType.ECOC;
	}
	
	public RestrictedStatus getRestrictedStatus(){
		return status;
	}
	
	public void setRestrictedStatus(RestrictedStatus latestStatus){
		this.keyValid = true;
		status = (RestrictedStatusECM)latestStatus;
	}
	
	public GpsLoc getGpsLocation(){
		if( status == null )
			 return new GpsLoc();
		 return (status.gpsLoc);
	}
	
	public ConveyanceID getConveyanceID(){
		if( status == null )
			 return new ConveyanceID();
		 return (status.coveyanceID);
	}
	
	public boolean getArmedStatus(){
		if( status == null )
			 return false;
		else return status.opMode == 1;
	}
	
	 public int getAlarmCount(){
		 int cnt = 0;
		 cnt += lockOpen() ? 1 : 0;
		 cnt += haspOpen() ? 1 : 0;
		 cnt += offCourse() ? 1 : 0;
		 cnt += offSchedule() ? 1 : 0;
		 
		 return cnt;
	 }
	 
	 public boolean lockOpen(){
		 if( status == null )
			 return false;
		 return ((status.alarmCode & BIT_7) > 0);
	 }
	 
	 public boolean haspOpen(){
		 if( status == null )
			 return false;
		 return ((status.alarmCode & BIT_6) > 0);
	 }
	 
	 public boolean offCourse(){
		 if( status == null )
			 return false;
		 return ((status.alarmCode & BIT_5) > 0);
	 }

	 public boolean offSchedule(){
		 if( status == null )
			 return false;
		 return ((status.alarmCode & BIT_4) > 0);
	 }
	 
	 
	 
	 public boolean timeNotSet(){
		 if( status == null )
			 return false;
		 return ((status.errorCode & BIT_2) > 0);
	 }
	 
	 public boolean commFailed(){
		 if( status == null )
			 return false;
		 return ((status.errorCode & BIT_5) > 0);
	 }
	 
	 public boolean insuficientPower(){
		 if( status == null )
			 return false;
		 return ((status.errorCode & BIT_6) > 0);
	 }
	 
	 public boolean configMalfunction(){
		 if( status == null )
			 return false;
		 return ((status.errorCode & BIT_7) > 0);
	 }
	 
	 
	 public boolean sensorMalfunction(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_7) > 0);
	 }
	 
	 public boolean decryptionError(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_6) > 0);
	 }
	 
	 public boolean invalidCommand(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_5) > 0);
	 }
	 
	 public boolean logOverflow(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_4) > 0);
	 }
	 
	 public boolean ackFailure(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_3) > 0);
	 }
	 
	 public boolean configFailure(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_2) > 0);
	 }
	 
	 public boolean sensorEnableFailure(){
		 if( status == null )
			 return false;
		 return ((status.errorBits & BIT_1) > 0);
	 }
}
