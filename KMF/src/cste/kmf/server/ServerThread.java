package cste.kmf.server;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import static cste.kmf.packet.PacketTypes.*;
import cste.icd.ICD;
import cste.kmf.KmfDeviceRecord;
import cste.kmf.KmfDeviceRecord.InvalidRecordExeption;
import cste.kmf.database.DbHandler;
import cste.kmf.packet.AddRecordPacket;
import cste.kmf.packet.PacketTypes;
import cste.kmf.packet.PacketTypes.*;


/*
 * http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 * http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
 * 
 */

public class ServerThread implements Runnable{
	private static final String TAG = ServerThread.class.getName();
    protected Socket clientSocket = null;
    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    
	public ServerThread(Socket socket) {
		System.out.println("Client connected");
        clientSocket = socket;
    }

	@Override
	public void run() {
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
        	System.err.println("Error initializing server socket");
        	return;
        }
        
        byte packetType = -1;
        System.out.println("waiting for data");
    	try {
			packetType = in.readByte();
		} catch (IOException e1) {
			System.err.println("Packet format error");
			return;
		}

		System.out.println("Received packet type " + packetType);
		switch(packetType){
		case ADD_RECORD:
			handleAddRecordPacket(in);
			break;
		case DELETE_RECORD:
			handleDeleteRecordPacket(in);
			break;
		case GENERATE_LTK:
			handleGenerateLTKPacket(in);
			break;
		case GENERATE_TCK:
			handleGenerateTCKPacket(in);
			break;
		default:
			System.out.println("Received an invalid packet type!");
			break;
		}
		
	}
	
	// first bye is status
	// 0 = OK
	// 1 = ERROR
	
	// next is the requested key (if needed)
	
	void sendACK(ObjectOutputStream out, byte[] key){
		//TODO 
	}
	
	void sendKey(ObjectOutputStream out, byte[] key){
		//TODO 
	}
	
	void handleGenerateLTKPacket(ObjectInputStream is){
		//TODO check ICD for proper inputs
		// send packet back to caller
		ICD.generateLTK(null, 0);
	}
	
	void handleGenerateTCKPacket(ObjectInputStream is){
		//TODO check ICD for proper inputs
		// send packet back to caller
		ICD.generateTCK(null, 0);
	}
	
	void handleDeleteRecordPacket(ObjectInputStream is){
		//TODO send ACK reply
		byte uid[] = new byte[UID_LENGTH];
		try {
			in.read(uid, 0, UID_LENGTH);
		} catch (IOException e) {
			System.err.println("Error reading delete record packet from socket!");
			return;
		}
		
		if ( DbHandler.deleteDeviceRecord(uid) ){
			
		}
	}
	
	void handleAddRecordPacket(ObjectInputStream is){
		//TODO send ACK reply
		byte uid[] = new byte[UID_LENGTH];
		byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
		byte type = PacketTypes.NO_TYPE;
		int rekeyAscNum = -1;
		
		try {
			type = in.readByte();
			in.read(uid, 0, UID_LENGTH);
			in.read(rekeyKey, 0, ENCRYPTION_KEY_LENGTH);
			rekeyAscNum = in.readInt();
		} catch (IOException e) {
			System.err.println("Error reading add record packet from socket!");
			return;
		}

		KmfDeviceRecord record;
		try {
			record = new KmfDeviceRecord(type,uid,rekeyKey,rekeyAscNum);
		} catch (InvalidRecordExeption e) {
			System.err.println("The device record is invalid");
			return;
		}
		
		if ( DbHandler.addDeviceRecord(record) ){
			
		}
	}
}
