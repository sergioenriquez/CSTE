package cste.kmf;

import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import cste.kmf.packet.PacketTypes;

/***
 * Represents device record information stored on the KMF database
 * @author Sergio Enriquez
 *
 */
public final class KmfDeviceRecord {
	protected final byte deviceType;
	protected final byte devUID[];
	protected final byte devRekeyKey[];
	protected final int ascCnt;
	
	public byte getDeviceType(){
		return deviceType;
	}
	
	public byte[] getUID(){
		return devUID;
	}
	
	public byte[] getRekeyKey(){
		return devRekeyKey;
	}
	
	public int getAscCount(){
		return ascCnt;
	}
	
	protected boolean isValid(){
		if ( !PacketTypes.isValid(deviceType) )
			return false;
		
		if ( devUID == null || devUID.length != UID_LENGTH )
			return false;
		
		if ( devRekeyKey == null || devRekeyKey.length != ENCRYPTION_KEY_LENGTH )
			return false;
		
		if ( ascCnt < 0)
			return false;
		
		return true;
	}
	
	public KmfDeviceRecord(byte type, byte[] uid, byte[] key, int asc) throws InvalidRecordExeption{
		deviceType = type;
		devUID = uid;
		devRekeyKey = key;
		ascCnt = 0;
		
		if ( !isValid())
			throw new InvalidRecordExeption();
	}
	
	public class InvalidRecordExeption extends Exception{
		private static final long serialVersionUID = 1L;};
}
