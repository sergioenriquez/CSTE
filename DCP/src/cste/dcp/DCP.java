package cste.dcp;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.dcp.kmf.KmfClient;
import cste.icd.DeviceTypes;
import cste.icd.ICD;

public class DCP {
	
	protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
	static final String TAG = DCP.class.getName();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		KmfClient k = new KmfClient("127.0.0.1",12345);
		
		
		
		
		NetDevice d1 = new NetDevice(
				DeviceTypes.KMF,
				Hex.unmarshal("F34DBB5490729865"),
				Hex.unmarshal("F65143C3652AF21962AA86C7B1E55B21"),
				Hex.unmarshal("5CFFEA7E13EA6FCC8045CB6F8D31B942"),
				0);
		
		NetDevice d2 = new NetDevice(
				DeviceTypes.CSD,
				Hex.unmarshal("FFEECCDDEEFFAABB"),
				Hex.unmarshal("1234BBBBCCCCDDDDEEEEFFFF00001111"));
		
		NetDevice d3 = new NetDevice(
				DeviceTypes.KMF,
				Hex.unmarshal("11112222EEFFAABB"),
				Hex.unmarshal("1234BBBBCCCCDDDDEEEEFFFF00001111"));
		
		//byte[] tck = ICD.generateTCK_L0(Hex.unmarshal("F65143C3652AF21962AA86C7B1E55B21"), Hex.unmarshal("F34DBB5490729865"), 1);
		
		byte[] enc = ICD.encryptAES(d3.getRekeyKey(), d3.getRekeyKey());
		byte[] enc2 = new byte[16];
		System.arraycopy(enc, 0, enc2, 0, 16);
		
		byte[] dec = null;

		dec = ICD.decryptAES(enc, d3.getRekeyKey());

//		
//		//k.addRecord(d1);
//		k.addRecord(d1);
//		k.addRecord(d2);
//		k.addRecord(d3);
		
//		k.addRecord(d1);
//		k.deleteRecord(d1);
		
		//k.getNewLTK(d1);
		
		//k.addRecord(Hex.unmarshal("0011223344556677"),Hex.unmarshal("00112233445566770011223344556677"));
		
		System.out.println("DCP exit");
	}

}
