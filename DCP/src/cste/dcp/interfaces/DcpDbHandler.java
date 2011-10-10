package cste.dcp.interfaces;

import cste.dcp.NetDevice;

public interface DcpDbHandler {
	//TODO: add methods
	boolean initialize();
	boolean addDeviceRecord(NetDevice device);
	boolean removeDeviceRecord(NetDevice device);
}
