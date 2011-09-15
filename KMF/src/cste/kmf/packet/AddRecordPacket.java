package cste.kmf.packet;
import static cste.kmf.packet.PacketTypes.*;
import static cste.icd.ICD.*;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AddRecordPacket {
	protected final byte type = ADD_RECORD;
	protected byte uid[] = new byte[UID_LENGTH];
	protected byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
	
	public AddRecordPacket(byte[] recordUID, byte[] recordRekeyKey){
		uid = recordUID;
		rekeyKey = recordRekeyKey;
	}

	public static AddRecordPacket readFromSocket(ObjectInputStream in){

		byte uid[] = new byte[UID_LENGTH];
		byte rekeyKey[] = new byte[ENCRYPTION_KEY_LENGTH];
		
		try {
			in.read(uid, 0, UID_LENGTH);
			in.read(rekeyKey, UID_LENGTH, ENCRYPTION_KEY_LENGTH);
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
}
