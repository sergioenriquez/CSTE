package cste.components;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class ECoC extends ComModule{
	public static final int STATUS_DATA_SIZE = 55;
	public ECoC(DeviceUID devUID) {
		super(devUID);
		this.devType = DeviceType.ECOC;
		statusData = new byte[STATUS_DATA_SIZE];
	}

}
