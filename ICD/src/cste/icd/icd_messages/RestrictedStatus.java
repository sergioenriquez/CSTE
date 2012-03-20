package cste.icd.icd_messages;

import java.io.Serializable;

import cste.icd.general.IcdPayload;

public abstract class RestrictedStatus extends IcdPayload implements Serializable{
	private static final long serialVersionUID = -5560824529074735687L;
	public byte errorCode;
	public byte ackNo;
	protected byte[] restrictedDataSection;
}