package cste.kmf.server;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import static cste.kmf.packet.PacketTypes.*;
import cste.icd.ICD;
import cste.kmf.KmfApp;
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
			handleAddRecordPacket();
			break;
		case DELETE_RECORD:
			handleDeleteRecordPacket();
			break;
		case GENERATE_LTK:
			handleGenerateLTKPacket();
			break;
		case GENERATE_TCK:
			handleGenerateTCKPacket();
			break;
		default:
			System.out.println("Received an invalid packet type!");
			break;
		}
		
	}
	
	void sendACK(boolean successful, byte[] key){
		try {
			out.writeBoolean(successful);
			if ( successful && key != null )
				out.write(key);
		} catch (IOException e) {
			System.err.println("Error sending ACK packet!");
		}
	}

	void handleGenerateLTKPacket(){
		byte[] deviceUID = new byte[UID_LENGTH];
		byte[] newLTK = null;
		
		try {
			in.read(deviceUID);
		} catch (IOException e) {
			System.err.println("error reading generate LTK packet!");
			return;
		}
		
		KmfDeviceRecord currentRecord = DbHandler.getDeviceRecord(deviceUID);
		
		if ( currentRecord == null){
			sendACK(false,null);
			return;
		}
		
		byte[] rekeyKey = currentRecord.getRekeyKey();

		byte[] currentLTK = currentRecord.getLTK();
		newLTK = ICD.encryptAES(currentLTK,rekeyKey);

		if ( newLTK == null){
			sendACK(false,null);
			return;
		}
		
		try {
			KmfDeviceRecord modifiedRecord = new KmfDeviceRecord(
					currentRecord.getDeviceType(),
					currentRecord.getUID(),
					currentRecord.getRekeyKey(),
					currentRecord.getRekeyCtr()+1,
					newLTK);
			
			DbHandler.addDeviceRecord(modifiedRecord);
		} catch (InvalidRecordExeption e) {
			System.err.println("Error updating device record!");
		}
		
		sendACK(true ,newLTK);
	}
	
	/***
	 * Device A is the sender, B is the receiver and can generate the TCK on its own
	 */
	void handleGenerateTCKPacket(){
		/**
		 * read UID device 1
		 * read UID device 2
		 * generate TCk depending on respective levels
		 */
		
		byte[] deviceUID_A = new byte[UID_LENGTH];
		byte[] deviceUID_B = new byte[UID_LENGTH];
		byte[] newTCK = null;
		
		try {
			in.read(deviceUID_A);
			in.read(deviceUID_B);
		} catch (IOException e) {
			System.err.println("error reading generate TCK packet!");
			return;
		}
		
		KmfDeviceRecord recordA = DbHandler.getDeviceRecord(deviceUID_A);
		KmfDeviceRecord recordB = DbHandler.getDeviceRecord(deviceUID_B);
		
		if ( recordA == null || recordB == null){
			sendACK(false,null);
			return;
		}
		
		int senderDevLvl = recordA.getDeviceLevel();
		byte[] generatedTCK = null;
		
		switch(senderDevLvl){
		case 0:
			generatedTCK = ICD.generateTCK_L0(
					recordB.getRekeyKey(), 
					KmfApp.getKmfUID(), 
					recordA.getRekeyCtr());
			break;
		case 1:
			generatedTCK = ICD.generateTCK_L1(
					recordA.getUID(),
					recordB.getLTK());
			break;
		default:
			// level 2 is generated by the DCP, so KMF is not involved
				break;
		}
		
		sendACK( generatedTCK==null?false:true ,generatedTCK);
	}
	
	void handleDeleteRecordPacket(){
		byte uid[] = new byte[UID_LENGTH];
		try {
			in.read(uid, 0, UID_LENGTH);
		} catch (IOException e) {
			System.err.println("Error reading delete record packet from socket!");
			return;
		}
		
		if ( DbHandler.deleteDeviceRecord(uid) )
			sendACK(true,null);
		else
			sendACK(false,null);
	}
	
	/***
	 * When added a new device, set LTK to all 0, and rekey counter to 0
	 * 
	 * @param is
	 */
	void handleAddRecordPacket(){

		byte uid[] = new byte[UID_LENGTH];
		byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
		byte devLTK[] = new byte[ENCRYPTION_KEY_LENGTH];
		byte type = PacketTypes.NO_TYPE;
		int rekeyCtr = 0;
		
		try {
			type = in.readByte();
			in.read(uid, 0, UID_LENGTH);
			in.read(rekeyKey, 0, ENCRYPTION_KEY_LENGTH);
			rekeyCtr = in.readInt();
			in.read(devLTK,0,ENCRYPTION_KEY_LENGTH);
		} catch (IOException e) {
			System.err.println("Error reading add record packet from socket!");
			return;
		}

		KmfDeviceRecord record;
		try {
			record = new KmfDeviceRecord(type,uid,rekeyKey,rekeyCtr,devLTK);
		} catch (InvalidRecordExeption e) {
			System.err.println("The device record is invalid");
			sendACK(false,null);
			return;
		}
		
		if ( DbHandler.addDeviceRecord(record) ){
			sendACK(true,null);
		}
	}
}
