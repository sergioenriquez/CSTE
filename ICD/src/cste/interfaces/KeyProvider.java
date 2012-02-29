package cste.interfaces;

import cste.icd.DeviceUID;

public interface KeyProvider {
	public byte[] getEncryptionKey(DeviceUID destinationUID);
}
