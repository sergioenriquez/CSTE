package cste.dcp;

import cste.dcp.kmf.KmfClient;

public class DCP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Dcp ran");
		KmfClient k = new KmfClient("127.0.0.1",12345);
		k.addRecord();
	}

}
