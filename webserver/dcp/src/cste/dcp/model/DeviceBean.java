package cste.dcp.model;
import static cste.dcp.model.Utility.*;

import java.io.Serializable;


public class DeviceBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int deviceID;
	private String deviceUID;
	private int deviceType;
	private String tck;
	private int tckAsc;
	private boolean isAssigned;
	
	public DeviceBean(){}
	
	public void setDeviceID(int val) {deviceID = val;}
    public int getDeviceID() { return deviceID;}
    
    public void setDeviceUID(byte[] val) {deviceUID = hexToStr(val);}
    public String getDeviceUID() { return deviceUID;}

    public void setDeviceType(int str) {deviceType = str;}
    public int getDeviceType() {return deviceType;}    

    public void setTck(byte[] val) {tck = hexToStr(val);}
    public String getTck() { return tck;}
    
    public void setTckAsc(int val) {tckAsc = val;}
    public int getTckAsc() { return tckAsc;}
    
    public void setIsAssigned(boolean val) {isAssigned = val;}
    public boolean getIsAssigned() { return isAssigned;}
}
