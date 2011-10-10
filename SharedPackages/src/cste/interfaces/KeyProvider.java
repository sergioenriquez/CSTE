package cste.interfaces;

public interface KeyProvider {
	public byte[] getEncryptionKey(byte[] destinationDevUID);
}
