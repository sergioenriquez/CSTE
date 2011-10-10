package cste.kmf;

import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.icd.DeviceType;

/***
 * Represents device record information stored on the KMF database
 * @author Sergio Enriquez
 *
 */
public final class KmfDeviceRecord {
	protected final DeviceType devTypeCode;
	protected final byte[] devUID;
	protected final byte[] devRekeyKey;
	protected final byte[] devLTK;
	protected final int rekeyCtr;
	
	protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
	public DeviceType getDeviceType(){
		return devTypeCode;
	}
	
	public byte[] getUID(){
		return devUID;
	}
	
	public byte[] getRekeyKey(){
		return devRekeyKey;
	}
	
	public byte[] getLTK(){
		return devLTK;
	}
	
	public int getRekeyCtr(){
		return rekeyCtr;
	}
	
	public int getDeviceLevel(){
		return devTypeCode.getLevel();
	}
	
	protected boolean isValid(){

		if ( devUID == null || devUID.length != UID_LENGTH )
			return false;
		
		if ( devRekeyKey == null || devRekeyKey.length != ENCRYPTION_KEY_LENGTH )
			return false;
		
		if ( devLTK == null || devLTK.length != ENCRYPTION_KEY_LENGTH )
			return false;
		
		if ( rekeyCtr < 0)
			return false;
		
		return true;
	}
	
	public KmfDeviceRecord(DeviceType type, byte[] uid, byte[] key, int asc, byte[] ltk) throws InvalidRecordExeption{
		devTypeCode = type;
		devUID = uid;
		devRekeyKey = key;
		rekeyCtr = 0;
		devLTK = ltk;
		
		if ( !isValid())
			throw new InvalidRecordExeption();
	}
	
	@Override
	public String toString(){
		String text = String.format("TYPE=%x , UID=%s , KEY=%s , CTR=%d , LTK=%s", 
				devTypeCode,
				Hex.marshal(devUID),
				Hex.marshal(devRekeyKey),
				rekeyCtr,
				Hex.marshal(devLTK));
		return text;
	}
	
	public class InvalidRecordExeption extends Exception{
		private static final long serialVersionUID = 1L;};
		
	
}
