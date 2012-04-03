package cste.dcp;

import cste.icd.general.Constants;
import cste.icd.types.DeviceType;

/**
 * Represents a secure container device or a NAD/FNAD
 * @author User
 *
 */
public class NetDevice {
	protected DeviceType devTypeCode;
	protected byte[] devUID;
	protected byte[] devRekeyKey;
	protected byte[] devLTK;
	protected int rekeyCtr;
	
	/***
	 * Used for existing devices
	 * @param devTypeCode
	 * @param devUID
	 * @param devRekeyKey
	 * @param devLTK
	 * @param rekeyCtr
	 */
	public NetDevice(
			DeviceType devTypeCode,
			byte[] devUID,
			byte[] devRekeyKey,
			byte[] devLTK,
			int rekeyCtr){
		this.devTypeCode = devTypeCode;
		this.devUID = devUID;
		this.devRekeyKey = devRekeyKey;
		this.devLTK = devLTK;
		this.rekeyCtr = rekeyCtr;
	}
	
	/***
	 * Used for new devices
	 * @param devTypeCode
	 * @param devUID
	 * @param devRekeyKey
	 * @param devLTK
	 * @param rekeyCtr
	 */
	public NetDevice(
			DeviceType devTypeCode,
			byte[] devUID,
			byte[] devRekeyKey
		){
		this.devTypeCode = devTypeCode;
		this.devUID = devUID;
		this.devRekeyKey = devRekeyKey;
		this.devLTK = Constants.generateLTK(devRekeyKey);
		this.rekeyCtr = 0;
	}

	public byte[] getUID(){
		return devUID;
	}
	
	public byte[] getRekeyKey(){
		return devRekeyKey;
	}
	
	public int getRekeyCtr(){
		return rekeyCtr;
	}
	
	public byte[] getLTK(){
		return devLTK;
	}
	
	public DeviceType getTypeCode(){
		return devTypeCode;
	}
}
