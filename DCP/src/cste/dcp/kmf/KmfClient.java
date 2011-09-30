package cste.dcp.kmf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.dcp.NetDevice;
import cste.icd.ICD;
import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import static cste.kmf.packet.PacketTypes.*;

public class KmfClient {
	private static final String TAG = KmfClient.class.getName();
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    protected static HexBinaryAdapter Hex = new HexBinaryAdapter(); //TODO move this to ICD 
    
	public KmfClient(String address,int port){
		kmfServerPort = port;
		kmfServerAddress = address;
	}
	
	protected boolean connectToServer(){
		System.out.println("Attempting to connect to server");
    	try {
			clientSocket = new Socket(kmfServerAddress,kmfServerPort);
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Server unreachable");
			return false;
		} catch (IOException e) {
			System.err.println("Error creating socket");
			return false;
		}
    	return true;
    }
	
	public void getNewLTK(NetDevice device){
		boolean success = false;
		byte[] receivedLTK = null;
		
		if( connectToServer() ){
			try {
				System.out.print("Sending request for LTK: ");
				out.writeByte(GENERATE_LTK);
				out.write(device.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
				if ( success ){
					in.read(receivedLTK, 0, ENCRYPTION_KEY_LENGTH);
					System.out.println("KEY: " + Hex.marshal(receivedLTK));
				}
				
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
	}
	
	public void getNewTCK(NetDevice deviceA,NetDevice deviceB){
		boolean success = false;
		if( connectToServer() ){
			try {
				System.out.print("Sending request for TCK: ");
				out.writeByte(GENERATE_TCK);
				out.write(deviceA.getUID());
				out.write(deviceB.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
	}
	
	public boolean deleteRecord(NetDevice device){
		boolean success = false;
		if( connectToServer() ){

			try {
				System.out.print("Sending request to delete record: ");
				out.writeByte(DELETE_RECORD);
				out.write(device.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
		return success;
	}
	
	public boolean addRecord(NetDevice device){
		boolean success = false;
		System.out.println(success);
		if( connectToServer() ){
			try {
				System.out.print("Sending request to add record: ");
				out.writeByte(ADD_RECORD);
				out.write(device.getTypeCode());
				out.write(device.getUID());
				out.write(device.getRekeyKey());
				out.writeInt(device.getRekeyCtr());
				byte[] deviceLTK = ICD.generateLTK(device.getRekeyKey());
				out.write(deviceLTK);
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
		return success;
	}
	
	protected void disconnectFromServer(){
    	try{
    		clientSocket.close();
		} catch (IOException e){
			System.err.println("Error disconnecting from server");
		}
    }
	
}
