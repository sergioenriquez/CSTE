package hnad.android.Miscellaneous;

import android.util.Log;

public class Utils {
	// For debugging
	private static final String TAG = Utils.class.getName();

	/**
	 * Method to encode a hex string into a byte array.
	 * @param s
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public static void hexStringToBytes(String s, byte[] bytes, int offset) {
		int len = s.length();
		for (int i = 0; i < len; i += 2) {
			bytes[i/2 + offset] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
		}
	}
	
	/**
	 * Method to encode a byte array into a hex string.
	 * @param bytes
	 * @param offset
	 * @param length
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes, int offset, int length) {
		String hex = "";
		for (int i = offset; i < offset + length; i++) {
			hex += Integer.toHexString((int)((bytes[i] >> 4) & 0xF)) + Integer.toHexString((int)(bytes[i] & 0xF));
		}
		return hex;
	}
	
	/**
	 * Convert bytes to an int.
	 * 
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public static int bytesToInt(byte[] bytes, int offset) {
		if (bytes == null || offset + 4 > bytes.length) {
			Log.e(TAG, "toInt() error");
			return 0;
		}
		return (int)((0xff & bytes[offset + 0]) << 24 |
				     (0xff & bytes[offset + 1]) << 16 |
				     (0xff & bytes[offset + 2]) <<  8 |
				     (0xff & bytes[offset + 3]) <<  0);
	}
	
	/**
	 * Copy an int into a byte array.
	 * 
	 * @param i
	 */
	public static void intToBytes(int i, byte[] bytes, int offset) {
		if (bytes == null || offset + 4 > bytes.length) {
			Log.e(TAG, "toBytes() error");
			return;
		}
		bytes[offset + 0] = (byte) ((i >> 24) & 0xff);
		bytes[offset + 1] = (byte) ((i >> 16) & 0xff);
		bytes[offset + 2] = (byte) ((i >>  8) & 0xff);
		bytes[offset + 3] = (byte) ((i >>  0) & 0xff);
	}
}
