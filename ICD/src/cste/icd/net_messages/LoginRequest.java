package cste.icd.net_messages;

import java.io.UnsupportedEncodingException;

import cste.icd.general.IcdPayload;

public class LoginRequest extends IcdPayload{
	public byte[]username;
	public byte[]password;
	
	public static final int FIELD_SIZE = 16;
	public static final int SECTION_SIZE = FIELD_SIZE+FIELD_SIZE;
	
	public LoginRequest(String username,String password){
		this.username = new byte[16];
		this.password = new byte[16];
		
		if( username.length() > FIELD_SIZE)
			username = username.substring(0, FIELD_SIZE);
		if( password.length() > FIELD_SIZE)
			password = password.substring(0, FIELD_SIZE);
		
		try {
			this.username = username.getBytes("UTF8");
			this.password = password.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			//TODO LOG
		}
	}
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getSize() {
		return SECTION_SIZE;
	}

}
