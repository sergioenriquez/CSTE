package cste.kmf;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class KmfServer implements Runnable{
	protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    
	public KmfServer(int port)
	{
		serverPort = port;
		
	}
	
	private synchronized boolean isStopped() {
        return isStopped;
    }
	
	private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }
	
	public synchronized void stop(){
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

	@Override
	public void run() {
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {

                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            new Thread(
                    new KmfServerThread(clientSocket)
                    ).start();
        }
	}
}
