package cste.kmf.server;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import static cste.kmf.packet.PacketTypes.*;
import cste.kmf.database.DbHandler;
import cste.kmf.packet.AddRecordPacket;
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

		System.out.println("packet type " + packetType);
		switch(packetType){
		case ADD_RECORD:
			handleAddRecordPacket(in);
			break;
		case DELETE_RECORD:
			break;
		case GENERATE_LTK:
			break;
		case GENERATE_TCK:
			break;
		default:
				break;
		}
		
	}
	
	private void handleAddRecordPacket(ObjectInputStream is){
		AddRecordPacket p = AddRecordPacket.readFromSocket(is);
		if ( p!=null){
			System.out.println("Receid add record packet:");
			System.out.println(p);
			
			DbHandler.addDeviceRecord(p.getUID(), p.getRekeyKey(), ADD_RECORD);
			
			AddRecordPacket x = DbHandler.getDeviceRecord(p.getUID());
			byte[] a = x.getUID();
		}
	}
}
