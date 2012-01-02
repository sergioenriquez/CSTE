package cste.android.core;
import static cste.android.activities.PreferencesActivity.PREFERENCES_FILE;
import static cste.android.activities.PreferencesActivity.SERVER_ADDRESS;
import static cste.android.activities.PreferencesActivity.SERVER_PORT;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.SharedPreferences;
import cste.PacketTypes.DcpPacketTypes;
import cste.PacketTypes.KmfPacketTypes;
import cste.hnad.HnadTcpClientInterface;
import cste.interfaces.IpWrapper;
import cste.ip.IpPacket;
import cste.ip.IpWrapperImpl;

public class NetworkHandler {
	private static String server = "";
	private static int port = 0;
	private static Service hostService; // TODO interface!
	private static SharedPreferences mSettings;
	private static IpWrapper ipWrapper;

	public static void setServiceHost(Service host){
		hostService = host;
		mSettings = host.getSharedPreferences(PREFERENCES_FILE, 0);
		
		// TODO init ipwrapper
	}
	
	public void loginToDCP(String username,String password){
		loadSettings();
		
		
		Runnable r = new HnadNetworkClientThread(username,password);
		new Thread(r).start();
		
		//new clientthread object, pass socket and callback
		// thread code here?
		//connect, send data, get reply
		//object executes callback of service host
	}

	
	protected void loadSettings(){
		server = mSettings.getString(SERVER_ADDRESS, "0.0.0.0");
		port = mSettings.getInt(SERVER_PORT, 0);
	}

	protected class HnadNetworkClientThread implements Runnable {

		String mUsername;
		String mPassword;
		int task;
		
		Socket s;
		ObjectOutputStream out;
		ObjectInputStream in;
		
		/***
		 * logs in to the dcp server
		 * @param username
		 * @param password
		 */
		public HnadNetworkClientThread(String username, String password){
			mUsername = username;
			mPassword = password;
			task = 1; // log in
		}
		
		protected boolean connectToServer() {
			loadSettings();
			// TODO Auto-generated method stub
			try {
				s = new Socket(server,port);
				in = new ObjectInputStream(s.getInputStream());
				out = new ObjectOutputStream(s.getOutputStream());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//log event and print to debug console
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//log event
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if ( !connectToServer() ){
				// report connection error
			}

			switch(task){
			case 1:
				short function = 0;
				byte []payload = null;
				byte []destinationUID = null; // TODO replace with deviceUID class
				
				ipWrapper.sendIcdPacket(function, payload, destinationUID, out);
				IpPacket p = ipWrapper.getReply(in);
				if ( p !=null && p.getFunctionCode() == DcpPacketTypes.OP_SUCCESS)
				{
					//report successful authentication
				}
				else
				{
					// report bad login
				}
					
				break;
			default:
			}
		}

	}

}
