package cste.dcp.kmf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import cste.kmf.packet.AddRecordPacket;

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
	
	public void addRecord(final byte[] recordUID, final byte[] rekeyKey){
		if( connectToServer() ){
			
			AddRecordPacket p = new AddRecordPacket(recordUID,rekeyKey);
			p.writeToSocket(out);
			disconnectFromServer();
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
