package cste.dcp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.interfaces.IpWrapper;
import cste.ip.IpWrapperImpl;

public class DcpServerThread implements Runnable{
	protected Socket clientSocket = null;
    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
    protected IpWrapper ipWrapper = new IpWrapperImpl();
    
    public DcpServerThread(Socket socket) {
		System.out.println("Client connected");
        clientSocket = socket;
        
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
