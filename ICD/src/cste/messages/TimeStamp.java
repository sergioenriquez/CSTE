package cste.messages;

import java.nio.ByteBuffer;
import java.util.Calendar;

public class TimeStamp {
	final byte month;
	final byte day;
	final byte year;
	final byte weekday;
	final byte hour;
	final byte minute;
	final byte second;
	final byte secFrac;
	
	public TimeStamp(
			int month,
			int day,
			int year,
			int weekday,
			int hour,
			int minute,
			int second,
			int secFrac){
		this.month = (byte)month;
		this.day = (byte)day;
		this.year = (byte)year;
		this.weekday = (byte)weekday;
		this.hour = (byte)hour;
		this.minute = (byte)minute;
		this.second = (byte)second;
		this.secFrac = (byte)secFrac;
	}
	
	public static TimeStamp blank(){
		return new TimeStamp(0,0,0,0,0,0,0,0);
	}
	
	public static TimeStamp now(){
		Calendar now = Calendar.getInstance();

		int month = 1+now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		int year =  (now.get(Calendar.YEAR)-2000);
		int weekday = now.get(Calendar.DAY_OF_WEEK)-1;
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int secFrac = (now.get(Calendar.MILLISECOND)/10);
		
		return new TimeStamp(month,day,year,weekday,hour,minute,second,secFrac);
	}
	
	public byte[] getBytes()
	{
		ByteBuffer temp = ByteBuffer.allocate(8);
		temp.put(month);
		temp.put(day);
		temp.put(year);
		temp.put(weekday);
		temp.put(hour);
		temp.put(minute);
		temp.put(second);
		temp.put(secFrac);
		return temp.array();
	}
}
