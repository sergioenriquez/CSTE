package cste.dcp;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.dcp.kmf.KmfClient;

public class DCP {
	
	protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
	static final String TAG = DCP.class.getName();
	/**
	 * @param args
	 */
	static void main(String[] args) {

		KmfClient k = new KmfClient("192.168.0.1",12345);
		
		//k.addRecord(Hex.unmarshal("0011223344556677"),Hex.unmarshal("00112233445566770011223344556677"));
		
		System.out.println("DCP exit");
	}

}
