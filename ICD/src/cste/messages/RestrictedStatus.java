package cste.messages;

public abstract class RestrictedStatus extends IcdPayload{
	protected byte errorCode;
	protected byte[] restrictedDataSection;
}
