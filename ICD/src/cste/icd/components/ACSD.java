package cste.icd.components;

import cste.icd.types.DeviceUID;

public class ACSD extends CSD{
	private static final long serialVersionUID = 2299784963129757435L;

	public ACSD(DeviceUID devUID, byte[] address) {
		super(devUID, address);
	}
}
