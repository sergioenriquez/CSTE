package cste.dcp;

/**
 * Represents a secure container device
 * @author User
 *
 */
public class Device {
	public byte[] getUID(){
		// TODO
		return null;
	}
	public byte[] getRekeyKey(){
		// TODO
		// this is optional since its needed only when 
		// csd tries to create a new device
		return null;
	}
	public int getRekeyAscCount(){
		// TODO
		// this is optional since its needed only when 
		// csd tries to create a new device
		return 0;
	}
	public byte[] getType(){
		// TODO
		return null;
	}
}
