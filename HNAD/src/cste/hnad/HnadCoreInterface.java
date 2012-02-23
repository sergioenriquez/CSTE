package cste.hnad;

import cste.messages.IcdMsg;
import cste.misc.ZigbeePkt;

public interface HnadCoreInterface {
	public void onPacketReceived(ZigbeePkt pkt); // TODO: fill in functions
	public void onUsbStateChanged(boolean state); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();
}
