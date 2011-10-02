package cste.icd;

public interface KeyProvider {
	public byte[] getEncryptionKey(byte[] destinationDevUID);
}
