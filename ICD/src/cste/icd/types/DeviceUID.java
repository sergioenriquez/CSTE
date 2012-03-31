package cste.icd.types;
import static cste.icd.general.Constants.UID_LENGTH;
import static cste.icd.general.Utility.hexToStr;
import static cste.icd.general.Utility.strToHex;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DeviceUID implements Serializable{

	private static final long serialVersionUID = 4245549146110063113L;
	static public final int SIZE = 8;
	final byte[] deviceUID;
	
	public static DeviceUID fromBuffer(ByteBuffer b){
		byte[] temp = new byte[UID_LENGTH];
		b.get(temp, 0, UID_LENGTH);
		return new DeviceUID(temp);
	}
	
	//TODO remove 
	public static DeviceUID fromByteArray(byte[] arr){
		return new DeviceUID(arr.clone());
	}

	//TODO remove 
	public static DeviceUID fromString(String uid){
		byte[] temp = strToHex(uid);
		return new DeviceUID(temp);
	}

	public DeviceUID(byte[] deviceUID){
		this.deviceUID = deviceUID;
	}
	
	public DeviceUID(String deviceUID){
		this.deviceUID = strToHex(deviceUID);
	}
	
	public boolean isValid(){
		if ( deviceUID == null || deviceUID.length != UID_LENGTH)
			return false;
		else
			return true;
	}
	
	public byte[] getBytes(){
		return deviceUID;
	}
	
	@Override 
	public int hashCode() {
	    return Arrays.hashCode(deviceUID);
	}
	
	@Override
	public boolean equals(Object  otherObj){
		if ( this == otherObj ) 
			return true;
		if ( !(otherObj instanceof DeviceUID) ) 
			return false;
		DeviceUID otherUID = (DeviceUID)otherObj;
		return Arrays.equals(otherUID.deviceUID, this.deviceUID);
	}
	
	@Override
	public String toString(){
		return hexToStr(deviceUID);
	}

}
