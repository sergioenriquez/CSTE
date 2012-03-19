package cste.icd.general;

//import org.bouncycastle2.crypto.InvalidCipherTextException;
//import org.bouncycastle2.crypto.engines.AESEngine;
//import org.bouncycastle2.crypto.modes.CCMBlockCipher;
//import org.bouncycastle2.crypto.params.CCMParameters;
//import org.bouncycastle2.crypto.params.KeyParameter;
//import org.bouncycastle2.openssl.EncryptionException;
//import org.bouncycastle2.util.encoders.Hex;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import cste.icd.types.DeviceType;

import static cste.icd.general.Utility.*;

@SuppressWarnings("unused")
public class Constants {	
	// Message encryption key length
	public static final int ENCRYPTION_KEY_LENGTH			= 16;

	// The ICD revision number this implements
	private static final byte ICD_REV_NUMBER 				= 0x02;

	// UTC Length per ICD
	private static final int UTC_LENGTH						= 8; // bytes
		
	// Message integrity check (MIC) length
	private static final int MIC_LENGTH 					=  8; // in bytes
	public  static final int UID_LENGTH						=  8; // in bytes

	// Message header lengths
	public static final int MH_ASCENSION_LENGTH				=  4;
	public static final int MH_HEADER_LENGTH 				= 16;

	// Restricted data section lengths (if more than one byte)
	private static final int MECHANICAL_SEAL_ID_LENGTH		= 15;
	private static final int CONVEYANCE_ID_LENGTH			= 16; // id itself is 15 since format takes 1 byte
	private static final int MANIFEST_LENGTH				= 16;
	private static final int RSM_RESTRICTED_DATA_LENGTH 	= 55;
	private static final int RSM_RESERVED_LENGTH			= 16;
	
	/***
	 * Method not specified on ICD, using this as a place holder
	 * @param deviceRekeyKey
	 * @return
	 */
	public static byte[] generateLTK(byte[] deviceRekeyKey){
		return encryptAES(deviceRekeyKey,deviceRekeyKey);
	}
	
	public static byte[] generateTCK_L0(
			byte[] receiverRekeyKey,
			byte[] KmfUID,
			int rekeyCtr)
	{
//		byte[] cipher = new byte[16];
//		cipher[0] = DeviceType.KMF.getBytes();
//		cipher[1] = MsgType.ENCRYPYION_REKEY.getBytes();
//		cipher[2] = 0; //TODO fill in value
//		System.arraycopy(KmfUID, 0, cipher, 3, UID_LENGTH);
//		cipher[11] = ICD_REV_NUMBER;
//		System.arraycopy(intToByteArray(rekeyCtr), 0, cipher, 12, 4);
//		
//		byte[] generatedKey = encryptAES(receiverRekeyKey,cipher);
//		String gen2 = hexToStr(generatedKey);
		return null;//generatedKey;
	}
	
	public static byte[] generateTCK_L1(
			byte[] dcpUID,  
			byte[] receiverLTK){
		
		byte[] cipher = new byte[16];
		cipher[0] = DeviceType.DCP.getBytes();
		cipher[1] = 0;
		cipher[2] = 0;
		System.arraycopy(dcpUID, 0, cipher, 3, UID_LENGTH);
		
		byte[] generatedKey = encryptAES(receiverLTK,cipher);
		return generatedKey;
	}

	public static byte[] generateTCK_L2(
			byte[] currentLev1TCK, 
			byte[] level2DevUID,
			byte level2DevType){

		byte[] cipher = new byte[16];
		cipher[0] = level2DevType;
		cipher[1] = 0;
		cipher[2] = 0;
		System.arraycopy(level2DevUID, 0, cipher, 3, UID_LENGTH);
		
		byte[] generatedKey = encryptAES(currentLev1TCK,cipher);
		return generatedKey;
	}
}
