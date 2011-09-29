package cste.dcp;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.dcp.kmf.KmfClient;
import cste.icd.DeviceTypes;

public class DCP {
	
	protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
	static final String TAG = DCP.class.getName();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		KmfClient k = new KmfClient("127.0.0.1",12345);
		
		NetDevice d1 = new NetDevice(
				DeviceTypes.CSD,
				Hex.unmarshal("AABBCCDDEEFFAABB"),
				Hex.unmarshal("AAAABBBBCCCCDDDDEEEEFFFF00001111"));
		
		k.addRecord(d1);
		/*
		dev 1 
		 */
		
		//k.addRecord(Hex.unmarshal("0011223344556677"),Hex.unmarshal("00112233445566770011223344556677"));
		
		System.out.println("DCP exit");
	}

}
