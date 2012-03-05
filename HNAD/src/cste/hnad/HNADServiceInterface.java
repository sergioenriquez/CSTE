package cste.hnad;

import android.content.SharedPreferences;
import cste.components.ComModule;
import cste.icd.DeviceUID;
import cste.misc.ZigbeeFrame;

public interface HNADServiceInterface {
	public void onPacketReceived(ZigbeeFrame pkt); // TODO: fill in functions
	public void onUsbStateChanged(boolean state); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();
	
	public void getDeviceStatus(DeviceUID destination);
	public void getDeviceLog(DeviceUID destination);
	public SharedPreferences getSettingsFile();
	public void logout(); 
}
