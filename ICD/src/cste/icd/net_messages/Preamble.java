package cste.icd.net_messages;

import cste.icd.types.DeviceUID;

public class Preamble {
	byte revision;
	short functionCode;
	DeviceUID devUID;
}
