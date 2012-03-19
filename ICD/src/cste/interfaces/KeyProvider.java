package cste.interfaces;

import cste.icd.types.DeviceUID;

public interface KeyProvider {
	public byte[] getEncryptionKey(DeviceUID destinationUID);
	//TODO store
}
