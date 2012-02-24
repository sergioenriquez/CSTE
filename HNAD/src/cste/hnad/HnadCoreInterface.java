package cste.hnad;

import cste.misc.ZigbeeFrame;

public interface HnadCoreInterface {
	public void onPacketReceived(ZigbeeFrame pkt); // TODO: fill in functions
	public void onUsbStateChanged(boolean state); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();
}
