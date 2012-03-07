package cste.messages;

import java.io.Serializable;

public abstract class RestrictedStatus extends IcdPayload implements Serializable{
	protected byte errorCode;
	protected byte[] restrictedDataSection;
}
