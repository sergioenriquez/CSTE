package cste.dcp.kmf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import cste.dcp.NetDevice;
import cste.icd.ICD;
import cste.icd.KeyProvider;
import cste.ip.IcdIpPacket;
import cste.ip.IcdIpWrapper;
import cste.ip.PacketTypes;
import static cste.ip.PacketTypes.*;
import static cste.dcp.DcpApp.*;

public class KmfClient implements KeyProvider{
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    protected static HexBinaryAdapter Hex = new HexBinaryAdapter(); //TODO Replace Hex with google guava library
    protected ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    
	public KmfClient(String address,int port){
		kmfServerPort = port;
		kmfServerAddress = address;
		IcdIpWrapper.setSenderUID(DCP_UID);
		IcdIpWrapper.setKeyProvider(this);
	}
	
	/***
	 * 
	 * @return
	 */
	protected boolean connectToServer(){
		System.out.println("Attempting to connect to server");
    	try {
			clientSocket = new Socket(kmfServerAddress,kmfServerPort);
			in = new ObjectInputStream(clientSocket.getInputStream());
			out = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Server unreachable");
			return false;
		} catch (IOException e) {
			System.err.println("Error creating socket");
			return false;
		}
    	return true;
    }
	
	/***
	 * 
	 */
//	protected void disconnectFromServer(){
//    	try{
//    		clientSocket.close();
//		} catch (IOException e){
//			System.err.println("Error disconnecting from server");
//		}
//    }
	
	/***
	 * 
	 * @param device
	 */
	public byte[] getNewLTK(NetDevice device){
		if( connectToServer() ){
			IcdIpWrapper.sendIcdPacket(GENERATE_LTK, device.getUID() , KMF_UID, out);
			IcdIpPacket p = IcdIpWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == PacketTypes.REPLY_KEY)
				return p.getPayload();
		}
		return null;
	}
	
	/***
	 * 
	 * @param deviceA
	 * @param deviceB
	 */
	public byte[] getNewTCK(NetDevice deviceA,NetDevice deviceB){
		if( connectToServer() ){
			
			bOut.reset();
			try {
				bOut.write(deviceA.getUID());
				bOut.write(deviceB.getUID());
			} catch (IOException e) {
				System.err.println("Error building packet payload");
				return null;
			}
			
			IcdIpWrapper.sendIcdPacket(GENERATE_TCK, bOut.toByteArray() , KMF_UID, out);
			
			IcdIpPacket p = IcdIpWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == PacketTypes.REPLY_KEY)
				return p.getPayload();
		}
		return null;
	}
	
	/***
	 * 
	 * @param device
	 * @return
	 */
	public boolean deleteRecord(NetDevice device){
		if( connectToServer() ){
			IcdIpWrapper.sendIcdPacket(DELETE_RECORD, device.getUID(), KMF_UID, out);
			IcdIpPacket p = IcdIpWrapper.getReply(in);
			
			if ( p !=null && p.getFunctionCode() == PacketTypes.OP_SUCCESS)
				return true;
		}
		return false;
	}

	/***
	 * 
	 * @param device
	 * @return
	 */
	public boolean addRecord(NetDevice device){
		if( connectToServer() ){
			bOut.reset();
			try {
				bOut.write(device.getTypeCode());
				bOut.write(device.getUID());
				bOut.write(device.getRekeyKey());
				bOut.write(device.getRekeyCtr());
				bOut.write( ICD.generateLTK(device.getRekeyKey() ) );
			} catch (IOException e) {
				System.err.println("Error sending packet");
				return false;
			}
			
			IcdIpWrapper.sendIcdPacket(ADD_RECORD, bOut.toByteArray() , KMF_UID, out);
			
			IcdIpPacket p = IcdIpWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == PacketTypes.OP_SUCCESS)
				return true;

		}
		return false;
	}

	@Override
	public byte[] getEncryptionKey(byte[] destinationDevUID) {
		// TODO Auto-generated method stub
		return DCP_LTK;
	}
}
