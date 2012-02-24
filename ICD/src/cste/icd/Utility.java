package cste.icd;

public class Utility {
	
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
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
