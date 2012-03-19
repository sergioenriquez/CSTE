package cste.hnad;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.content.SharedPreferences;
import cste.android.core.HNADService.DeviceCommands;
import cste.icd.components.ComModule;
import cste.icd.icd_messages.EventLogICD;
import cste.icd.types.DeviceUID;
import cste.misc.XbeeFrame;

public interface HNADServiceInterfaceOLD {
	public void onFrameReceived(XbeeFrame pkt); // TODO: fill in functions
	public void onUsbStateChanged(boolean state); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();

	public void sendDevCmd(DeviceUID destination, DeviceCommands cmd);
	
	public void onRadioTransmitResult(boolean success, DeviceUID destUID, short ackNo);
	
	public Hashtable<DeviceUID,ComModule> getDeviceList();
	public ArrayList<EventLogICD> getDeviceEventLog(DeviceUID devUID);
	public void deleteDeviceRecord(DeviceUID devUID);
	public ComModule getDeviceRecord(DeviceUID devUID);
	public void deleteDeviceLogs(DeviceUID devUID);
	public SharedPreferences getSettingsFile();
	public void logout(); 
	public Context getContext();
	public void setDeviceAssensionVal(DeviceUID dev, int val);
	
	public void setDeviceTCK(DeviceUID devUID, byte []newTCK);
	
	public void test(); //temp
}
