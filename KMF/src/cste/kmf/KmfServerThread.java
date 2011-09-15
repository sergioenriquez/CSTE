package cste.kmf;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import static cste.kmf.KmfPacketTypes.*;
import cste.kmf.KmfPacketTypes.*;


/*
 * http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 * http://tutorials.jenkov.com/java-multithreaded-servers/thread-pooled-server.html
 * 
 */

public class KmfServerThread implements Runnable{
	
    protected Socket clientSocket = null;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;
    
	public KmfServerThread(Socket clientSocket) {
		
        clientSocket = clientSocket;

    }

	@Override
	public void run() {
		
		
		try {
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
				
        } catch (IOException e) {
        	int packetType = -1;
        	
        	try {
				packetType = in.readInt();
			} catch (IOException e1) {
				System.err.println("Packet format error");
				return;
			}
			/*
			KmfPacketTypes.AddRecordPacket p = new AddRecordPacket();
			p.type = 10;
			try {
				out.writeObject(p);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			*/
			
			switch(packetType)
			{
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
		
	}
	
	private void handleAddRecordPacket(ObjectInputStream is)
	{
		
	}
}
