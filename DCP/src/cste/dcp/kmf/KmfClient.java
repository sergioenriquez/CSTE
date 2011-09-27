package cste.dcp.kmf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cste.dcp.Device;
import static cste.kmf.packet.PacketTypes.*;

public class KmfClient {
	private static final String TAG = KmfClient.class.getName();
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    
	public KmfClient(String address,int port){
		kmfServerPort = port;
		kmfServerAddress = address;
	}
	
	protected boolean connectToServer(){
    	try {
			clientSocket = new Socket(kmfServerAddress,kmfServerPort);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Server unreachable");
			return false;
		} catch (IOException e) {
			System.err.println("Error creating socket");
			return false;
		}
    	return true;
    }
	
	protected void disconnectFromServer(){
    	try{
    		out.close();
    		in.close();
    		clientSocket.close();
		} catch (IOException e){
			System.err.println("Error disconnecting from server");
		}
    }
	
	public void addRecord(Device device){
		if( connectToServer() ){

			try {
				out.writeByte(ADD_RECORD);
				out.write(device.getType());
				out.write(device.getUID());
				out.write(device.getRekeyKey());
				out.writeInt(device.getRekeyAscCount());
			} catch (IOException e) {
				// TODO Auto-generated catch block

			}
			finally{
				disconnectFromServer();
			}
			
		}
	}
	
}

/*
 
 new Thread(new Runnable(){
			public void run()
			{
			
			}
}.start();
 */ 
