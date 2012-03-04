package cste.icd;
import static cste.icd.Constants.UID_LENGTH;
import static cste.icd.Utility.*;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class DeviceUID implements Serializable{
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
		this.deviceUID = hexStringToByteArray(deviceUID);
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
		return hexToStr(deviceUID);
	}

	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
