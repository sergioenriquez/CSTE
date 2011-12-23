package cste.android.core;
import static cste.android.activities.PreferencesActivity.PREFERENCES_FILE;
import static cste.android.activities.PreferencesActivity.SERVER_ADDRESS;
import static cste.android.activities.PreferencesActivity.SERVER_PORT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import android.content.SharedPreferences;
import cste.hnad.HnadTcpClientInterface;
import cste.interfaces.IpWrapper;
import cste.ip.IpWrapperImpl;

public class NetworkHandler implements Runnable{
	private static String server = "";
	private static int port = 0;
	private static Service hostService; // TODO interface!
	private static SharedPreferences mSettings;
	
	Socket s;
	OutputStream out;
	InputStream in;

	public static void setServiceHost(Service host){
		hostService = host;
		mSettings = host.getSharedPreferences(PREFERENCES_FILE, 0);
	}
	
	public void loginToDCP(String username,String password){
		//new clientthread object, pass socket and callback
		// thread code here?
		//connect, send data, get reply
		//object executes callback of service host
	}

	
	protected void loadSettings(){
		server = mSettings.getString(SERVER_ADDRESS, "0.0.0.0");
		port = mSettings.getInt(SERVER_PORT, 0);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	protected boolean connectToServer() {
		loadSettings();
		// TODO Auto-generated method stub
		try {
			s = new Socket(server,port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
