package cste.icd.types;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class GpsLoc implements Serializable{
	private static final long serialVersionUID = 7693667833332135085L;
	public static final int SIZE = 20;
	private byte[] gpsLocation =null;
	
	
	public GpsLoc(){
		gpsLocation = new byte[SIZE];
	}
	
	public GpsLoc(ByteBuffer b){
		this();
		if(b.remaining() >= SIZE )
			b.get(gpsLocation);
	}
	
	public GpsLoc(String str){
		this();
		try {
			if( str.length() > SIZE)
				str = str.substring(0, SIZE);
			gpsLocation = str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		return gpsLocation;
	}
	
	@Override
	public String toString(){
		if(gpsLocation==null)
			return "";
		
		try {
			return  new String(gpsLocation, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			return "error";
		}
	}
}
