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
import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;
import static cste.ip.PacketTypes.*;
import static cste.dcp.DcpApp.*;

public class KmfClient implements KeyProvider{
	private static final String TAG = KmfClient.class.getName();
	protected int kmfServerPort = 0;
	protected String kmfServerAddress = "";
	protected Socket clientSocket = null;
	protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;
    protected static HexBinaryAdapter Hex = new HexBinaryAdapter(); //TODO move this to ICD 
    
	public KmfClient(String address,int port){
		kmfServerPort = port;
		kmfServerAddress = address;
		IcdIpWrapper.setSenderUID(DCP_UID);
		IcdIpWrapper.setKeyProvider(this);
	}
	
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
	
	public void getNewLTK(NetDevice device){
		boolean success = false;
		byte[] receivedLTK = null;
		
		if( connectToServer() ){
			try {
				System.out.print("Sending request for LTK: ");
				out.writeByte(GENERATE_LTK);
				out.write(device.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
				if ( success ){
					in.read(receivedLTK, 0, ENCRYPTION_KEY_LENGTH);
					System.out.println("KEY: " + Hex.marshal(receivedLTK));
				}
				
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
	}
	
	public void getNewTCK(NetDevice deviceA,NetDevice deviceB){
		boolean success = false;
		if( connectToServer() ){
			try {
				System.out.print("Sending request for TCK: ");
				out.writeByte(GENERATE_TCK);
				out.write(deviceA.getUID());
				out.write(deviceB.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
	}
	
	public boolean deleteRecord(NetDevice device){
		boolean success = false;
		if( connectToServer() ){

			try {
				System.out.print("Sending request to delete record: ");
				out.writeByte(DELETE_RECORD);
				out.write(device.getUID());
				out.flush();
				success = in.readBoolean();
				System.out.println(success);
			} catch (IOException e) {
				System.err.println("Error sending packet");
			}
			finally{
				disconnectFromServer();
			}
		}
		return success;
	}
	
	byte[] buildAddRecordPayload(NetDevice device){
		
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		byte[] deviceLTK = ICD.generateLTK(device.getRekeyKey());
		try {
		b.write(ADD_RECORD);
		b.write(device.getTypeCode());
		b.write(device.getUID());
		b.write(device.getRekeyKey());
		b.write(device.getRekeyCtr());
		b.write(deviceLTK);
		} catch (IOException e) {
			System.err.println("Error sending packet");
		}
		
		return b.toByteArray();
	}
	
	void encryptPayloadAndSend(byte[] payload){
		byte[] payloadWithNonce = new byte[ICD.UID_LENGTH + payload.length];
		System.arraycopy(DCP_UID, 0, payloadWithNonce, 0, UID_LENGTH);
		System.arraycopy(payload, 0, payloadWithNonce, UID_LENGTH, payload.length);
		byte[] encryptedPayloadWithNonce = ICD.encryptAES(payloadWithNonce, DCP_LTK);
		
		try {
			out.write(DCP_UID);
			out.writeInt(encryptedPayloadWithNonce.length);
			out.write(encryptedPayloadWithNonce);
			out.flush();
		} catch (IOException e) {
			System.err.println("Error sending payload");
		}
	}
	
	boolean getResponse(byte[] reply){
		boolean success = false;
		int replyLen = 0;
		try {
			success = in.readBoolean();
			if ( success && reply != null){
				replyLen = in.readInt();
				in.read(reply,0,replyLen);
			}
		} catch (IOException e) {
			System.err.println("Error reading reply");
		}
		
		return success;
	}
	
	public boolean addRecord(NetDevice device){
		if( connectToServer() ){
			byte[] payload = buildAddRecordPayload(device);
			IcdIpWrapper.sendIcdPacket(ADD_RECORD, payload, KMF_UID, out);
			IcdIpPacket p = IcdIpWrapper.getReply(in);
			
			if ( p !=null && p.getFunctionCode() == PacketTypes.OP_SUCCESS)
				return true;
			else
				return false;
		}
		else 
			return false;

//		boolean success = false;
//		if( connectToServer() ){
//			try {
//				System.out.print("Sending request to add record: ");
//				out.writeByte(ADD_RECORD);
//				out.write(device.getTypeCode());
//				out.write(device.getUID());
//				out.write(device.getRekeyKey());
//				out.writeInt(device.getRekeyCtr());
//				byte[] deviceLTK = ICD.generateLTK(device.getRekeyKey());
//				out.write(deviceLTK);
//				out.flush();
//				success = in.readBoolean();
//				System.out.println(success);
//			} catch (IOException e) {
//				System.err.println("Error sending packet");
//			}
//			finally{
//				disconnectFromServer();
//			}
//		}
//		return success;
		
	}
	
	protected void disconnectFromServer(){
    	try{
    		clientSocket.close();
		} catch (IOException e){
			System.err.println("Error disconnecting from server");
		}
    }

	@Override
	public byte[] getEncryptionKey(byte[] destinationDevUID) {
		// TODO Auto-generated method stub
		return DCP_LTK;
	}
}
