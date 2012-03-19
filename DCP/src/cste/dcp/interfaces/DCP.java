package cste.dcp.interfaces;

import cste.notused.NetPkt;

public interface DCP {
	/***
	 * 
	 * @param msg	An object with the message received. IP payload is already decrypted if applicable.
	 * @return	An optional reply object to be returned to the original packet sender
	 */
	public NetPkt processMsg(NetPkt msg);
}
