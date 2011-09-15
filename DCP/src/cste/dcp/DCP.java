package cste.dcp;

import cste.dcp.kmf.KmfClient;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;


public class DCP {
	private static final String TAG = DCP.class.getName();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Dcp ran");
		KmfClient k = new KmfClient("192.168.0.2",12345);
		
		
		HexBinaryAdapter x = new HexBinaryAdapter();
		
		
		byte[] uid = x.unmarshal("0011223344556677");
		byte[] rekeyKey = x.unmarshal("00112233445566770011223344556677");
		
		k.addRecord(uid,rekeyKey);
		
		System.out.println("Main exit");
	}

}
