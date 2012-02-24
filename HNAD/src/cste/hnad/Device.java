package cste.hnad;

public class Device {
	public int rssi;
	public boolean visible;
	public String uid;
	public String type;
	public String sealID;
	public String manifestID;
	public String conveyanceID;
	
	boolean alarmOn;
	boolean doorOpen;
	boolean opMode;
	
	public void setRssi(int v)
	{
		this.rssi = v;
	}
	
	public Device(boolean visible, String uidStr, String typeStr){
		this.rssi = 0;
		this.sealID = "NA";
		this.manifestID = "NA";
		this.conveyanceID = "NA";
		this.alarmOn = false;
		this.doorOpen = false;
		this.opMode = false;
		
		this.visible = visible;
		this.uid = uidStr;
		this.type = typeStr;
	}
}
