package cste.dcp;

import cste.dcp.kmf.KmfClient;

public class DCP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Dcp ran");
		KmfClient k = new KmfClient("192.168.0.2",12345);
		k.addRecord();
	}

}
