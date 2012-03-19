package cste.android.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Service;
import cste.android.core.HNADService;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.types.MaintenaceCode;
import cste.interfaces.IpWrapper;
import cste.notused.DcpPacketTypes;
import cste.notused.NetPkt;

public class NetworkHandler{
	public static String serverAddress;
	public static int serverPort;
	
	private HNADService hostService;
	//private static SharedPreferences mSettings;
	private static IpWrapper ipWrapper;

	public static void config(Service host, String server, int port){
		//hostService = host;
		//mSettings = host.getSharedPreferences(PREFERENCES_FILE, 0);
		
		// TODO init ipwrapper
	}
	
	public NetworkHandler(HNADService host){
		hostService = host;
		serverAddress = "";
		serverPort = 0;
	}
	
	public void sendIcdMsg(IcdMsg msg){
		loadSettings();
		
		NetPkt pkt = null;
		Runnable r = new HnadClientThread(pkt);//preamble + payload
		new Thread(r).start();

	}
	
	public void sendHeartBeat(MaintenaceCode code){
		loadSettings();
		
		NetPkt pkt = null;
		Runnable r = new HnadClientThread(pkt);//preamble + payload
		new Thread(r).start();

	}
	
	public void loginToDCP(String username,String password){
		loadSettings();
		
		NetPkt pkt = null;
		Runnable r = new HnadClientThread(pkt);//preamble + payload
		new Thread(r).start();

	}
	
	protected void loadSettings(){
		//server = mSettings.getString(SERVER_ADDRESS, "0.0.0.0");
		//port = mSettings.getInt(SERVER_PORT, 0);
	}
}
