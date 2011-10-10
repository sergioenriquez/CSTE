/**
 * 
 */
package cste.kmf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.icd.DeviceType;
import cste.kmf.KmfDeviceRecord.InvalidRecordExeption;
import cste.kmf.database.KmfDbHandler;
import cste.kmf.server.Server;

/**
 * @author Sergio Enriquez
 *
 */
public class KmfApp {
	static HexBinaryAdapter Hex = new HexBinaryAdapter();
	public static final String FACILITY_UID_STR = "F34DBB5490729865";
	public static final byte[] KMF_UID = Hex.unmarshal(FACILITY_UID_STR);
	
	
	public static byte[] getKmfUID(){
		return Hex.unmarshal(FACILITY_UID_STR);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args){
		int port;
		if ( args.length == 1){
			port = Integer.parseInt(args[0]);
		}
		else{
			System.out.println("Usage: run <port>");
			System.out.println("Defaulting to port 12345");
			port = 12345;
		}		
		
		if ( !KmfDbHandler.init() ){
			System.err.println("Error starting the database, closing...");
			return;
		}
			

		Server server = new Server(port);
		new Thread(server).start();
		
		System.out.println("Press enter to exit.");

		try {
			KmfDeviceRecord r = new KmfDeviceRecord(
					DeviceType.DCP,
					Hex.unmarshal("F34DBB5490729865"),
					Hex.unmarshal("FFEECCDDEEFFAABBFFEECCDDEEFFAABB"),
					0,
					Hex.unmarshal("11112222EEFFAABBFFEECCDDEEFFAABB"));
			KmfDbHandler.addDeviceRecord(r);
		} catch (InvalidRecordExeption e1) {

		}
		
		displayAllRecords();
		
		try {
			System.in.read();
		} catch (IOException e) {}

		server.stop();
		System.out.println("Server stopped.");
	}
	
	static void displayAllRecords(){
		List<KmfDeviceRecord> records = KmfDbHandler.getRecords();
		Iterator<KmfDeviceRecord> it = records.iterator();
		System.out.println( "Device records table has " + records.size() + " entries" );
		while(it.hasNext()){
			System.out.println( it.next()  );
		}
	}

}
