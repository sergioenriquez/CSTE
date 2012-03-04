package cste.dcp.kmf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import cste.PacketTypes.KmfPacketTypes;
import cste.dcp.NetDevice;
import cste.dcp.interfaces.KmfClient;
import cste.icd.DeviceUID;
import cste.icd.Constants;
import cste.interfaces.IpWrapper;
import cste.interfaces.KeyProvider;
import cste.ip.IpPacket;
import cste.ip.IpWrapperImpl;
import static cste.PacketTypes.KmfPacketTypes.*;
import static cste.dcp.DcpApp.*;

//TODO move the key provider functionality to the database handler
public class KmfClientImpl implements KmfClient,KeyProvider{
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    protected static HexBinaryAdapter Hex = new HexBinaryAdapter(); //TODO Replace Hex with google guava library
    protected ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    protected IpWrapper ipWrapper = new IpWrapperImpl();
    
	public KmfClientImpl(String address,int port){
		kmfServerPort = port;
		kmfServerAddress = address;
		ipWrapper.setSenderUID(DCP_UID);
		ipWrapper.setKeyProvider(this);
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
			ipWrapper.sendIcdPacket(GENERATE_LTK, device.getUID() , KMF_UID, out);
			IpPacket p = ipWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == KmfPacketTypes.REPLY_KEY)
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
			
			ipWrapper.sendIcdPacket(GENERATE_TCK, bOut.toByteArray() , KMF_UID, out);
			
			IpPacket p = ipWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == KmfPacketTypes.REPLY_KEY)
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
			ipWrapper.sendIcdPacket(DELETE_RECORD, device.getUID(), KMF_UID, out);
			IpPacket p = ipWrapper.getReply(in);
			
			if ( p !=null && p.getFunctionCode() == KmfPacketTypes.OP_SUCCESS)
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
				bOut.write(device.getTypeCode().getBytes());
				bOut.write(device.getUID());
				bOut.write(device.getRekeyKey());
				bOut.write(device.getRekeyCtr());
				bOut.write( Constants.generateLTK(device.getRekeyKey() ) );
			} catch (IOException e) {
				System.err.println("Error sending packet");
				return false;
			}
			
			ipWrapper.sendIcdPacket(ADD_RECORD, bOut.toByteArray() , KMF_UID, out);
			
			IpPacket p = ipWrapper.getReply(in);
			if ( p !=null && p.getFunctionCode() == KmfPacketTypes.OP_SUCCESS)
				return true;

		}
		return false;
	}

	@Override
	public byte[] getEncryptionKey(DeviceUID destinationUID) {
		// TODO Auto-generated method stub
		return DCP_LTK;
	}
}
