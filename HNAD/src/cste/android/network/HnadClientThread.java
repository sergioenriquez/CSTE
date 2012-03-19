package cste.android.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cste.notused.NetPkt;

public class HnadClientThread implements Runnable{
	
	
	int task;
	
	Socket s;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	/***
	 * logs in to the dcp server
	 * @param username
	 * @param password
	 */
	public HnadClientThread(NetPkt pkt){
	//	mUsername = username;
	//	mPassword = password;
		task = 1; // log in
	}
	
	protected boolean connectToServer() {

		// TODO Auto-generated method stub
		try {
			s = new Socket(NetworkHandler.serverAddress,NetworkHandler.serverPort);
			in = new ObjectInputStream(s.getInputStream());
			out = new ObjectOutputStream(s.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//log event and print to debug console
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//log event
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		if ( !connectToServer() ){
			// report connection error
		}

		switch(task){
		case 1:
			short function = 0;
			byte []payload = null;
			byte []destinationUID = null; // TODO replace with deviceUID class
			
//			ipWrapper.sendIcdPacket(function, payload, destinationUID, out);
//			NetPkt p = ipWrapper.getReply(in);
//			if ( p !=null && p.getFunctionCode() == DcpPacketTypes.OP_SUCCESS)
//			{
//				//report successful authentication
//			}
//			else
//			{
//				// report bad login
//			}
				
			break;
		default:
		}
	}

}
