package cste.icd;

/***
 * Generic Security Device
 * @author Sergio Enriquez
 *
 */
public class SecurityDevice {
	protected DeviceUID devUID;
	protected DeviceType devType;
	protected int rxAscension;
	protected int txAscension;

	protected String sealID;
	protected String manifestID;
	protected String conveyanceID;
	
	protected boolean alarmOn;
	protected boolean doorOpen;
	protected boolean opMode;
	
	public DeviceUID UID()
	{
		return devUID;
	}
	
	public DeviceType devType()
	{
		return devType;
	}
	
	public int getRxAsc(){
		return txAscension;
	}
	
	public int getTxAsc(){
		return txAscension;
	}
	
	
	public void setRxAsc(int rxAscension){
		this.rxAscension = rxAscension;
	}
	
	public void setTxAsc(int txAscension){
		this.txAscension = txAscension;
	}
	
	public void incTxAsc(){
		txAscension++;
	}
	
	public void incRxAsc(){
		rxAscension++;
	}
	
	public SecurityDevice(DeviceUID devUID, DeviceType devType ){
		this.devUID = devUID;
		this.devType = devType;
		this.rxAscension = 1;
		this.txAscension = 1;
		this.sealID = "NA1";
		this.manifestID = "NA2";
		this.conveyanceID = "NA3";
		this.alarmOn = false;
		this.doorOpen = false;
		this.opMode = false;
	}
}
