package cste.kmf;

import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import cste.icd.DeviceTypes;
import cste.kmf.packet.PacketTypes;

/***
 * Represents device record information stored on the KMF database
 * @author Sergio Enriquez
 *
 */
public final class KmfDeviceRecord {
	protected final byte devTypeCode;
	protected final byte[] devUID;
	protected final byte[] devRekeyKey;
	protected final byte[] devLTK;
	protected final int rekeyCtr;
	
	public byte getDeviceType(){
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
		return 0;
	}
	
	protected boolean isValid(){
		if ( !DeviceTypes.isValid(devTypeCode) )
			return false;
		
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
	
	public KmfDeviceRecord(byte type, byte[] uid, byte[] key, int asc, byte[] ltk) throws InvalidRecordExeption{
		devTypeCode = type;
		devUID = uid;
		devRekeyKey = key;
		rekeyCtr = 0;
		devLTK = ltk;
		
		if ( !isValid())
			throw new InvalidRecordExeption();
	}
	
	public class InvalidRecordExeption extends Exception{
		private static final long serialVersionUID = 1L;};
}
