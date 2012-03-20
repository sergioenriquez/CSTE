package cste.notused;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cste.interfaces.KeyProvider;

public interface IpWrapper {
	void setKeyProvider(KeyProvider keyProvider);
	void setSenderUID(byte[] uid);
	void sendIcdPacket(short function, byte[] payload, byte[] destinationUID, ObjectOutputStream out);
	NetPkt getReply(ObjectInputStream in);
}
