package cste.icd.net_messages;

import cste.icd.types.DeviceUID;

public class Preamble {
	public byte revision;
	public FunctionCode functionCode;
	public DeviceUID devUID;
	
	public Preamble(byte revision, FunctionCode functionCode, DeviceUID srcUID){
		this.revision = revision;
		this.functionCode = functionCode;
		devUID = srcUID;
	}
}
