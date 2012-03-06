package cste.hnad;

import android.content.SharedPreferences;
import cste.android.core.HNADService.DeviceCommands;
import cste.components.ComModule;
import cste.icd.DeviceUID;
import cste.misc.XbeeFrame;

public interface HNADServiceInterface {
	public void onFrameReceived(XbeeFrame pkt); // TODO: fill in functions
	public void onUsbStateChanged(boolean state); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();

	public void sendDevCmd(DeviceUID destination, DeviceCommands cmd);
	
	
	public SharedPreferences getSettingsFile();
	public void logout(); 
}
