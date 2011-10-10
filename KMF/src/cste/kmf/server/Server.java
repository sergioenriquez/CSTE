package cste.kmf.server;


import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import cste.interfaces.KeyProvider;
import cste.ip.IpWrapperImpl;
import cste.kmf.KmfDeviceRecord;
import cste.kmf.database.DbHandler;
import static cste.kmf.KmfApp.KMF_UID;

public class Server implements Runnable, KeyProvider{
	private static final String TAG = Server.class.getName();
	protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    
	public Server(int port){
		serverPort = port;
		IpWrapperImpl.setSenderUID(KMF_UID);
		IpWrapperImpl.setKeyProvider(this);
	}
	
	private synchronized boolean isStopped() {
        return isStopped;
    }
	
	private boolean openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
	
	public synchronized void stop(){
        isStopped = true;
        
        try {
        	if (serverSocket != null)
        		serverSocket.close();
        } catch (IOException e) {
        	System.err.println("Error closing server");
        }
    }

	@Override
	public void run(){
		synchronized(this){
            runningThread = Thread.currentThread();
        }
		
        if ( !openServerSocket() ){
        	System.err.println("Could not open server on port " + serverPort);
        	return;
        }
        else
        	System.out.println(String.format("KMF Server started on port %d",serverPort));
        
        while(!isStopped()){
            Socket clientSocket = null;
            
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e){
                if(isStopped()) 
                    return;
                
                System.err.println("Error accepting client connection");
                continue;
            }
            
            new Thread(new ServerThread(clientSocket)).start();
        }
	}

	@Override
	public byte[] getEncryptionKey(byte[] destinationDevUID) {
		// TODO Auto-generated method stub
		KmfDeviceRecord r = DbHandler.getDeviceRecord(destinationDevUID);
		return r.getLTK();
	}
}
