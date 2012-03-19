package cste.icd.types;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ConveyanceID implements Serializable{
	private static final long serialVersionUID = 797128188954708528L;
	public static final int SIZE = 16;
	private byte[] conveyanceID =null;
	
	
	public ConveyanceID(){
		conveyanceID = new byte[SIZE];
	}
	
	public ConveyanceID(ByteBuffer b){
		this();
		if(b.remaining() >= SIZE )
			b.get(conveyanceID);
	}
	
	public ConveyanceID(String str){
		this();
		try {
			if( str.length() > SIZE)
				str = str.substring(0, SIZE);
			conveyanceID = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		return conveyanceID;
	}
	
	@Override
	public String toString(){
		if(conveyanceID==null)
			return "";
		
		try {
			return  new String(conveyanceID, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			return "error";
		}
	}
}
