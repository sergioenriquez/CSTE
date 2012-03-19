package cste.icd.components;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import cste.icd.icd_messages.RestrictedStatus;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import static cste.icd.general.Utility.Log;
import static cste.icd.general.Utility.hexToStr;
import static cste.icd.general.Utility.strToHex;
/***
 * Generic Security Device
 * @author Sergio Enriquez
 *
 */
public abstract class ComModule implements Serializable{
	private static String TAG = "ComModule class";
	private static final long serialVersionUID = 2340395912239234229L;
	public DeviceUID devUID;
	public byte[] address;
	public DeviceType devType;
	public int rxAscension; 
	public int txAscension;
	protected byte[] tck = null;
	
	protected static byte[] BAD_KEY = strToHex("00000000000000000000000000000000");
	
	public byte rssi;	
	public boolean inRange;
	public boolean keyValid;

	public abstract int getAlarmCount();

	public abstract RestrictedStatus getRestrictedStatus();

	public abstract boolean getArmedStatus();
	
	public byte[] getTCK(){
		if( keyValid )
			return tck;
		else
			return new byte[16];
	}
	
	public boolean haveKey(){
		return keyValid;
	}

	protected byte icdRev; //TODO
	
	public abstract void setRestrictedStatus(RestrictedStatus latestStatus);

	public ComModule(DeviceUID devUID, byte[] address){
		this.devUID = devUID;
		this.rxAscension = 1;
		this.txAscension = 1;
		this.icdRev = 0x02;
		this.keyValid = false;
		this.tck = new byte[16];
		if( address != null && address.length == 8)
			this.address = address;
		else 
			this.address = devUID.getBytes();
		
		this.rssi = 0;
		this.inRange = false;
	}
	
	public String getTckStr(){
		if(tck==null)
			return "";
		else
			return hexToStr(tck);
	}
	
	public void setTCK(byte []newTCK){
		if( newTCK.length == 16 && !Arrays.equals(newTCK, BAD_KEY)){
			tck = newTCK;
			keyValid = true;
		}else{
			keyValid = false;
		}
	}
	
	/***
	 * Return the binary representation of this object
	 * @param cm The CM object to serialize
	 * @return Byte array, or null if there was an error
	 */
	public static byte[] serialize(ComModule cm){
		byte []result = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(cm); 
			out.close();
			result = bos.toByteArray();
		} catch (IOException e) {
			Log(TAG,e.getMessage());
		} 
		return result;
	}
	
	 public static ComModule deserialize(byte[] b) { 
		 try{ 
			ByteArrayInputStream bais = new ByteArrayInputStream(b);
			ObjectInputStream in = new ObjectInputStream(bais); 
			Object object = in.readObject(); 
			in.close(); 
			return (ComModule)object; 
	    } catch(ClassNotFoundException cnfe) { 
			Log(TAG, "class not found error"); 
			return null; 
	    } catch(IOException ioe) { 
		    Log(TAG, "io error"); 
		    return null; 
	    } 
    } 
	
	static final byte BIT_0 = 0x01;
	static final byte BIT_1 = 0x02;
	static final byte BIT_2 = 0x04;
	static final byte BIT_3 = 0x08;
	static final byte BIT_4 = 0x10;
	static final byte BIT_5 = 0x20;
	static final byte BIT_6 = 0x40;
	static final byte BIT_7 = (byte)0x80;
}
