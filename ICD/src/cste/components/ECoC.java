package cste.components;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;

public class ECoC extends ComModule{
	public ECoC(DeviceUID devUID) {
		super(devUID);
		this.devType = DeviceType.ECOC;
	}

}
