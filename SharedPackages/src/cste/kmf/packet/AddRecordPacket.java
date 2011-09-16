package cste.kmf.packet;
import static cste.kmf.packet.PacketTypes.*;
import static cste.icd.ICD.*;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class AddRecordPacket {
	protected final byte type = ADD_RECORD;
	protected byte uid[] = new byte[UID_LENGTH];
	protected byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
	protected static HexBinaryAdapter Hex = new HexBinaryAdapter();
	
	public byte[] getUID(){
		return uid;
	}
	
	public byte[] getRekeyKey(){
		return rekeyKey;
	}
	
	public AddRecordPacket(byte[] recordUID, byte[] recordRekeyKey){
		uid = recordUID;
		rekeyKey = recordRekeyKey;
	}
	
	public AddRecordPacket(String recordUID, String recordRekeyKey){
		uid = Hex.unmarshal(recordUID);
		rekeyKey = Hex.unmarshal(recordRekeyKey);
	}

	public static AddRecordPacket readFromSocket(ObjectInputStream in){

		byte uid[] = new byte[UID_LENGTH];
		byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
		
		try {
			in.read(uid, 0, UID_LENGTH);
			in.read(rekeyKey, 0, ENCRYPTION_KEY_LENGTH);
		} catch (IOException e) {
			System.err.println("Error reading add record packet");
			return null;
		}
		
		return new AddRecordPacket(uid,rekeyKey);
	}
	
	public void writeToSocket(ObjectOutputStream out){
		try {
			out.writeByte(type);
			out.write(uid);
			out.write(rekeyKey);
		} catch (IOException e) {
			System.err.println("Error writting packet to socket");
		}
	}
	
	@Override
	public String toString(){
		String s = "UID: 0x" + Hex.marshal(uid) + "  REKEY: 0x" + Hex.marshal(rekeyKey);
		return s;
	}
}
