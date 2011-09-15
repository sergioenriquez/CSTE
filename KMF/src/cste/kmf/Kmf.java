/**
 * 
 */
package cste.kmf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import cste.kmf.server.Server;

/**
 * @author Sergio Enriquez
 *
 */
public class Kmf {
	private static final String TAG = Kmf.class.getName();

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
