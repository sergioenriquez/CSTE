package cste.hnad;

import android.content.Context;
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
	public void onRadioTransmitResult(boolean success, byte[] destination);
	
	public void deleteDeviceRecord(DeviceUID devUID);
	public ComModule getDeviceRecord(DeviceUID devUID);
	public SharedPreferences getSettingsFile();
	public void logout(); 
	public Context getContext();
	public void setDeviceAssensionVal(DeviceUID dev, int val);
}
