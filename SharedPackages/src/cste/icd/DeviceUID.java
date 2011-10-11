package cste.icd;
import static cste.icd.ICD.UID_LENGTH;
import static cste.icd.ICD.Hex;

import java.nio.ByteBuffer;

public class DeviceUID {
	final byte[] deviceUID;
	
	public static DeviceUID fromBuffer(ByteBuffer b){
		byte[] temp = new byte[UID_LENGTH];
		b.get(temp, 0, UID_LENGTH);
		return new DeviceUID(temp);
	}
	
	public static DeviceUID fromByteArray(byte[] arr){
		return new DeviceUID(arr.clone());
	}
	
	public static DeviceUID fromString(String uid){
		byte[] temp = Hex.unmarshal(uid);
		return new DeviceUID(temp);
	}
	
	DeviceUID(byte[] deviceUID){
		this.deviceUID = deviceUID;
	}
	
	public boolean isValid(){
		if ( deviceUID == null || deviceUID.length != UID_LENGTH)
			return true;
		else
			return false;
	}
	
	public byte[] getBytes(){
		return deviceUID;
	}
	
	@Override
	public String toString(){
		return Hex.marshal(deviceUID);
	}
}
