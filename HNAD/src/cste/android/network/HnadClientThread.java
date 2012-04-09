package cste.android.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

import cste.notused.NetPkt;

public class HnadClientThread implements Runnable{
	private static final String TAG = "Network Thread";
	private final int BUFFER_SIZE = 2000;
	
	protected Socket socket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected NetworkManager nh;
	protected boolean isEnabled;

	public HnadClientThread(NetworkManager nh){
		this.nh = nh;
		this.isEnabled = true;
		socket = null;
	}
	
	protected boolean connectToServer() {
		try {
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			return true;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	void closeSocket(){
		try {
			if(socket != null)
				socket.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not connect to server!");
		}
	}

	@Override
	public void run() {
		try {
			socket = new Socket(nh.serverAddress, nh.serverPort);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage());
			isEnabled = false;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			isEnabled = false;
		}
		
		byte []buffer = new byte[BUFFER_SIZE];
		
		while(isEnabled){
			
			if( nh.getPendingPktCount() == 0)
				closeSocket();
				
			NetPkt pkt = nh.getNextPkt(); //will block until there is something
			NetPkt replyPkt;
			
			if( pkt == null){
				Log.e(TAG, "Error retriving next pkt to send");
				break;
			}
			
			if ( !socket.isConnected() && !connectToServer() ){
				Log.e(TAG, "Could not connect to server!");
				break;			
			}
				
			try {
				out.write(pkt.getBytes());
				if(pkt.replyExpected){
					in.read(buffer,0,BUFFER_SIZE);
					replyPkt = NetPkt.fromByteArray(buffer);
					nh.onPktReceived(replyPkt);
				} else
					replyPkt = null;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		closeSocket();
	}

}