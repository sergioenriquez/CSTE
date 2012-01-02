package cste.hnad;

import cste.messages.IcdMsg;

public interface HnadCoreInterface {
	public void packetReceived(IcdMsg msg); // TODO: fill in functions
	public void login(String username,String password);
	public void uploadData();
	
}
