package cste.ip;

import static cste.ip.PacketTypes.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import cste.icd.ICD;
import cste.icd.KeyProvider;
import static cste.icd.ICD.ENCRYPTION_KEY_LENGTH;
import static cste.icd.ICD.UID_LENGTH;

public class IcdIpWrapper {
	static final byte ICD_REVISION = (byte)0x01;
	static byte[] senderUID;
	static byte[] encryptionKey;
	static KeyProvider kp;
	
	static public void setKeyProvider(KeyProvider keyProvider){
		kp = keyProvider;
	}
	
	static public void setSenderUID(byte[] uid){
		senderUID = uid;
	}

	static public void sendIcdPacket(short function, byte[] payload, byte[] destinationUID, ObjectOutputStream out){
		int payloadSize = 0;
		byte[] payloadSent = null;
		
		if( payload != null){
			if( PacketTypes.encryptionUsed(function)){
				byte[] payloadWithNonce = new byte[payload.length + UID_LENGTH];
				System.arraycopy(senderUID, 0, payloadWithNonce, 0, UID_LENGTH);
				System.arraycopy(payload, 0, payloadWithNonce, UID_LENGTH, payload.length);
				byte[] encryptedPayloadWithNonce = ICD.encryptAES(payloadWithNonce, kp.getEncryptionKey(destinationUID));
				payloadSize = encryptedPayloadWithNonce.length;
				payloadSent = encryptedPayloadWithNonce;
			}
			else{
				payloadSize = payload.length;
				payloadSent = payload;
			}
		}

		try {
			out.writeByte(ICD_REVISION);
			out.writeShort(function);
			out.write(senderUID, 0, UID_LENGTH);
			out.writeInt(payloadSize);
			if (payloadSize>0)
				out.write(payloadSent, 0, payloadSize);
			out.flush();
		} catch (IOException e) {
			// TODO Handle IO error
		}
	}
	
	static public IcdIpPacket getReply(ObjectInputStream in){
		byte rev = 0;
		short functionCode = 0;
		byte[] senderUID = new byte[UID_LENGTH];
		byte[] receivedPayload = null;
		int payloadsize = 0;
		
		try {
			rev = (byte)in.read();
			//TODO check packet revision matches
			if ( rev != ICD_REVISION){
				return null;
			}
			
			functionCode = in.readShort();
			in.read(senderUID, 0, UID_LENGTH);
			payloadsize = in.readInt();
			receivedPayload = new byte[payloadsize];
			in.read(receivedPayload, 0, payloadsize);
		} catch (IOException e) {
			// handle IO error
			return null;
		}

		IcdIpPacket p = new IcdIpPacket();
		p.setRev(rev);
		p.setFunctionCode(functionCode);
		p.setSenderUID(senderUID);
		
		if( PacketTypes.encryptionUsed(functionCode)){
			byte[] payloadWithNonce = ICD.decryptAES(receivedPayload, kp.getEncryptionKey(senderUID));
			byte[] nonce = new byte[UID_LENGTH];
			byte[] payload = new byte[receivedPayload.length-UID_LENGTH];
			System.arraycopy(payloadWithNonce, 0, nonce, 0, UID_LENGTH);
			System.arraycopy(payloadWithNonce, UID_LENGTH, payload, 0, payload.length);
			if ( Arrays.equals(nonce, senderUID )){
				p.setPayload(payload);
			}else
			{
				return null;
			}
		}else
			p.setPayload(receivedPayload);
		
		return p;
	}
}
