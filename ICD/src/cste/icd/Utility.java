package cste.icd;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle2.crypto.engines.AESEngine;
import org.bouncycastle2.crypto.modes.CCMBlockCipher;
import org.bouncycastle2.crypto.params.CCMParameters;
import org.bouncycastle2.crypto.params.KeyParameter;


public class Utility {
	private static final String TAG = "ICD Utility functions";
	
	public static String hexToStr(byte[] msg) {
		if( msg == null || msg.length == 0)
			return "";
		
        StringBuilder sb = new StringBuilder();
        for (byte b : msg) {
            sb.append(String.format("%1$02X", b));
        }
        return sb.toString();
    }
	
	public static byte[] strToHex(String s) {
        int len = s.length();
        if (len <= 1 )
        	return new byte[1];
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
	
	private static final int KEY_LENGTH = 16;
	private static final int MIC_LENGTH = 8;
	private static CCMBlockCipher cipher = new CCMBlockCipher(new AESEngine());
	
	public static int getEncryptedSize(int messageLen){
		byte []key = new byte[KEY_LENGTH];
		byte []nonce = new byte[MIC_LENGTH];
		cipher.init(true, new CCMParameters(new KeyParameter(key), MIC_LENGTH * 8, nonce, null));
		return cipher.getOutputSize(messageLen);
	}
	
	public static byte[] encrypt(byte[] message, byte[] key, byte[] nonce) {
		byte[] encrypted = null;
		cipher.init(true, new CCMParameters(new KeyParameter(key), MIC_LENGTH * 8, nonce, null));
		try {
			encrypted = new byte[cipher.getOutputSize(message.length)];
			int len = cipher.processBytes(message, 0, message.length, encrypted, 0);
			cipher.doFinal(encrypted, len);

		} catch (Exception e) {
			Log(TAG,e.getMessage());
		}
		return encrypted;
	}
	//buffered block cypher
	public static byte[] decrypt(byte[] message, byte[] key, byte[] nonce) {
		byte[] encrypted = null;
		cipher.init(false, new CCMParameters(new KeyParameter(key), MIC_LENGTH * 8, nonce, null));
		try {
			encrypted = new byte[cipher.getOutputSize(message.length)];
			int len = cipher.processBytes(message, 0, message.length, encrypted, 0);
			cipher.doFinal(encrypted, len);
		} catch (Exception e) {
			Log(TAG,e.getMessage());
		}
		return encrypted;
	}

	public static byte[] encryptAES(byte[] message, byte[] encryptionKey){
		Key key = new SecretKeySpec(encryptionKey, "AES");
		Cipher c;

		try {
			boolean padMessaage = message.length % 16 == 0 ? false : true;
			if( padMessaage )
				c = Cipher.getInstance("AES/ECB/PKCS5Padding ");
			else
				c = Cipher.getInstance("AES/ECB/NoPadding");
			
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encValue = c.doFinal(message);
			return encValue;
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getCause().getMessage());
		} catch (NoSuchPaddingException e) {
			System.err.println(e.getCause().getMessage());
		} catch (InvalidKeyException e) {
			System.err.println(e.getCause().getMessage());
		} catch (IllegalBlockSizeException e) {
			System.err.println(e.getCause().getMessage());
		} catch (BadPaddingException e) {
			System.err.println(e.getCause().getMessage());
		}
		
		return null;
	}
	
	public static byte[] decryptAES(byte[] message, byte[] encryptionKey){
        Key key = new SecretKeySpec(encryptionKey, "AES");
        Cipher c;
        byte[] decValue = null;
		try {
			c = Cipher.getInstance("AES/CCM/NoPadding");
			c.init(Cipher.DECRYPT_MODE, key);
	        decValue = c.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getCause().getMessage());
		} catch (NoSuchPaddingException e) {
			System.err.println(e.getCause().getMessage());
		} catch (InvalidKeyException e) {
			System.err.println(e.getCause().getMessage());
		} catch (IllegalBlockSizeException e) {
			System.err.println(e.getCause().getMessage());
		} catch (BadPaddingException e) {
			System.err.println(e.getCause().getMessage());
		}finally{
			
		}
		return decValue;
    }
	
	public static void Log(String TAG, String msg){
		Logger.getLogger(TAG).log(Level.SEVERE,msg);
	}
}
