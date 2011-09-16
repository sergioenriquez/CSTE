package cste.dcp;

import cste.dcp.kmf.KmfClient;

public class DCP {
	private static final String TAG = DCP.class.getName();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		KmfClient k = new KmfClient("192.168.0.1",12345);

		k.addRecord("0011223344556677","00112233445566770011223344556677");
		
		System.out.println("DCP exit");
	}

}
