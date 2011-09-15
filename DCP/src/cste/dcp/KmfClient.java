package cste.dcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class KmfClient {
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
	
	public KmfClient(String address,int port)
	{
		kmfServerPort = port;
		kmfServerAddress = address;
		try {
			clientSocket = new Socket(kmfServerAddress,kmfServerPort);
			
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("1");
			out.writeInt(40);
			out.close();
			
				
		} catch (UnknownHostException e) {
			System.err.println("Server unreachable");
		} catch (IOException e) {
			System.err.println("Error creating kmf client socket");
		}
		System.out.println("constructor exit");
	}
	
	public void addRecord()
	{
		
	}
}
