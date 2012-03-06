package cste.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.messages.RestrictedStatus;
import static cste.icd.Utility.*;
import static cste.icd.Constants.*;
/***
 * Generic Security Device
 * @author Sergio Enriquez
 *
 */
public abstract class ComModule implements Serializable{
	private static String TAG = "ComModule class";
	private static final long serialVersionUID = 2340395912239234229L;
	protected DeviceUID devUID;
	protected DeviceType devType;
	protected int rxAscension; //for encrypted msg
	protected int txAscension; //for encrypted msg
	
	protected RestrictedStatus latestStatus = null;

	public byte[] tck; //TODO
	public byte[] ltk; //TODO
	
	protected byte icdRev; //TODO
	
	public void setRestrictedStatus(RestrictedStatus latestStatus){
		this.latestStatus = latestStatus;
	}

	public ComModule(DeviceUID devUID){
		this.devUID = devUID;
		this.rxAscension = 1;
		this.txAscension = 1;
		this.icdRev = 0x02;
		
		tck = new byte[ENCRYPTION_KEY_LENGTH];
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
			Log(TAG,"io error");
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
	
	public DeviceUID UID()
	{
		return devUID;
	}
	
	public DeviceType devType()
	{
		return devType;
	}
	
	public int getRxAsc(){
		return txAscension;
	}
	
	public int getTxAsc(){
		return txAscension;
	}
	
	public void setRxAsc(int rxAscension){
		this.rxAscension = rxAscension;
	}
	
	public void setTxAsc(int txAscension){
		this.txAscension = txAscension;
	}
	
	public void incTxAsc(){
		txAscension++;
	}
	
	public void incRxAsc(){
		rxAscension++;
	}
}