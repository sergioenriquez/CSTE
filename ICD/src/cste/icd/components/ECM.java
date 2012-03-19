package cste.icd.components;

import cste.icd.types.DeviceUID;

public class ECM extends ECoC{
	private static final long serialVersionUID = 7714617967246571523L;

	public ECM(DeviceUID devUID, byte[] address) {
		super(devUID, address);
	}

}
