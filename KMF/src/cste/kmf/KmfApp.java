/**
 * 
 */
package cste.kmf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.kmf.database.DbHandler;
import cste.kmf.server.Server;

/**
 * @author Sergio Enriquez
 *
 */
public class KmfApp {
	static final String FACILITY_UID_STR = "AABBCCDDEEFFAABB";
	static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
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
		
		DbHandler.init();
		
		Server server = new Server(port);
		new Thread(server).start();
		
		System.out.println("Press enter to exit.");

		try {
			System.in.read();
		} catch (IOException e) {}

		server.stop();
		System.out.println("Server stopped.");
	}

}
