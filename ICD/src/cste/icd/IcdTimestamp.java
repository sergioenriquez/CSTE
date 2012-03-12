package cste.icd;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IcdTimestamp implements Serializable{
	final byte month;
	final byte day;
	final byte year;
	final byte weekday;
	final byte hour;
	final byte minute;
	final byte second;
	final byte secFrac;
	
	public IcdTimestamp(ByteBuffer b){
		this.month = b.get();
		this.day = b.get();
		this.year = b.get();
		this.weekday = b.get();
		this.hour = b.get();
		this.minute = b.get();
		this.second = b.get();
		this.secFrac = b.get();
	}
	
	public IcdTimestamp(byte [] data){
		this(ByteBuffer.wrap(data));
	}
	
	public IcdTimestamp(
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
	
	public Calendar toCalendar(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.YEAR, year+2000);
		cal.set(Calendar.DAY_OF_WEEK, weekday+1);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, secFrac*10);
		return cal;
	}
	
	public static IcdTimestamp blank(){
		return new IcdTimestamp(0,0,0,0,0,0,0,0);
	}
	
	public static IcdTimestamp now(){
		Calendar now = Calendar.getInstance();

		int month = 1+now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		int year =  (now.get(Calendar.YEAR)-2000);
		int weekday = now.get(Calendar.DAY_OF_WEEK)-1;
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		int secFrac = (now.get(Calendar.MILLISECOND)/10);
		
		return new IcdTimestamp(month,day,year,weekday,hour,minute,second,secFrac);
	}
	
	public byte[] getBytes(){
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
	
	public String toString(){
		
		DateFormat df = new SimpleDateFormat("MMM dd, HH:mm:ss");
		Date date = toCalendar().getTime(); 
		return df.format(date);
	}
}
