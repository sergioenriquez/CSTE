package cste.components;

import cste.icd.MsgType;
import cste.icd.EcocCmdType;
import cste.icd.SecurityDevice;
import cste.messages.IcdMsg;


public class DCP {
	public static IcdMsg sendNOP(SecurityDevice destination)
	{
		return IcdMsg.create(destination, MsgType.RESTRICTED_STATUS_MSG, EcocCmdType.NOP);
	}
}
