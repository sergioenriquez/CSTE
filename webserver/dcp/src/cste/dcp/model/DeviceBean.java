package cste.dcp.model;

public class DeviceBean {
	private int deviceID;
	private String deviceUID;
	private int deviceType;
	private String tck;
	private int tckAsc;
	
	public DeviceBean(){}
	
	public void setDeviceID(int val) {deviceID = val;}
    public int getDeviceID() { return deviceID;}
    
    public void setDeviceUID(String str) {deviceUID = str;}
    public String getDeviceUID() { return deviceUID;}

    public void setDeviceType(int str) {deviceType = str;}
    public int getDeviceType() {return deviceType;}    

    public void setTck(String val) {tck = val;}
    public String getTck() { return tck;}
    
    public void setTckAsc(int val) {tckAsc = val;}
    public int getTckAsc() { return tckAsc;}
}
