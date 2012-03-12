package cste.messages;


public abstract class IcdPayload {
		
	public abstract byte[] getBytes();
	public abstract String toString();
	public abstract byte getSize();
}
