package cste.dcp;


import static cste.dcp.DcpApp.DCP_UID;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import cste.interfaces.IpWrapper;
import cste.interfaces.KeyProvider;
import cste.ip.IpWrapperImpl;
import cste.dcp.DcpServerThread;

public class DcpServer implements Runnable, KeyProvider{
	protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected IpWrapper ipWrapper = new IpWrapperImpl();
    
    public DcpServer(int port){
		serverPort = port;
		ipWrapper.setSenderUID(DCP_UID);
		ipWrapper.setKeyProvider(this);
	}
    
	@Override
	public byte[] getEncryptionKey(byte[] destinationDevUID) {
		// TODO Auto-generated method stub
		return null;
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
	public void run() {
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
            
            new Thread(new DcpServerThread(clientSocket)).start();
        }
	}

}
