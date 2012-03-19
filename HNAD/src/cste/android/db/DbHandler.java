package cste.android.db;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import cste.icd.components.ComModule;
import cste.icd.icd_messages.EventLogCSD;
import cste.icd.icd_messages.EventLogECM;
import cste.icd.icd_messages.EventLogICD;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import cste.icd.types.IcdTimestamp;
import cste.icd.types.NadEventLogType;
import cste.misc.HnadEventLog;

public class DbHandler {
	private static String TAG = "Db handler";
	private SQLiteDatabase db;
	private final Context context;
	private final DBhelper dbHelper;
	
	/*******************
	 * GENERAL METHODS *
	 *******************/
	
	/***
	 * Constructor creates the database file if needed
	 * @param c
	 */
	public DbHandler(Context c){
		context = c;
		dbHelper = new DBhelper(context,DbConst.DB_NAME,null,DbConst.DB_VERSION);
	}
	
	/**
	 * Creates and initializes the database file
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException{
		try{
			db = dbHelper.getWritableDatabase();
		}catch(SQLiteException ex){
			Log.e(TAG, ex.getMessage());
			db = dbHelper.getWritableDatabase();
		}
	}
	
	/**
	 * Closes the database file
	 */
	public void close(){
		db.close();
	}
	
	/*******************
	 * DEV METHODS     *
	 *******************/
	
	/***
	 * @param device
	 * @return
	 */
	public synchronized long storeDevice(ComModule device){
		String uid = device.devUID.toString();
		byte []data = ComModule.serialize(device);
		
		if ( data == null )
			return -1;
		
		ContentValues newDeviceVal = new ContentValues();
		newDeviceVal.put(DbConst.DEV_KEY_ID, uid);	
		newDeviceVal.put(DbConst.DEV_DATA, data);	
		
		return db.insertWithOnConflict(DbConst.DEVICE_TABLE, null, newDeviceVal, SQLiteDatabase.CONFLICT_REPLACE );
	}
	
	/***
	 * Retrieves a CM device object, or returns null if it does not exist
	 * @param devUID
	 * @return
	 */
	public synchronized ComModule getDevice(DeviceUID devUID){
		if( devUID == null )
    		return null;
		
		ComModule cm = null;
		String uid = devUID.toString();
		
		Cursor c = db.query(DbConst.DEVICE_TABLE, 
				null, // all columns
				DbConst.DEV_KEY_ID + " = ?", 
				new String[]{uid}, 
				null, 
				null,
				null);

		if (c.moveToFirst())
			cm = createCmFromCursor(c);
		c.close();
		
		return cm;
	}
	
	public int deleteDeviceRecord(DeviceUID devUID){
		String uid = devUID.toString();
		return db.delete(DbConst.DEVICE_TABLE, DbConst.DEV_KEY_ID + " = ?",  new String[]{uid});
	}
	
	public void resetTempDeviceVars(){
		Hashtable<DeviceUID,ComModule> tempMap = getStoredDevices();
		Enumeration<ComModule> devices = tempMap.elements();
		while(devices.hasMoreElements()){
			ComModule cm = devices.nextElement();
			cm.inRange = false;
    		cm.rssi = 0;
    		storeDevice(cm);
		}
	}
	
	public Hashtable<DeviceUID,ComModule> getStoredDevices(){
		Hashtable<DeviceUID, ComModule> deviceMap = new Hashtable<DeviceUID,ComModule>(8);
		Cursor c = db.query(DbConst.DEVICE_TABLE, 
				null, // all columns
				null, 
				null, 
				null, 
				null,
				null);

		c.moveToFirst();
        while (c.isAfterLast() == false) {
        	ComModule cm = createCmFromCursor(c);
        	if(cm == null)
        		Log.e(TAG, "Unable to retrieve CM record from database");
        	else{
        		//reset some values that should not be stored between sessions
        		//cm.inRange = false;
        		//cm.rssi = 0;
        		//cm.pendingTxMsgCnt = 0;
        		deviceMap.put(cm.devUID, cm);
        	}
            c.moveToNext();
        }
        c.close();
		
		return deviceMap;
	}
	
	private ComModule createCmFromCursor(Cursor c){
		String uid = c.getString(c.getColumnIndex(DbConst.DEV_KEY_ID));
        byte[] data = c.getBlob(c.getColumnIndex(DbConst.DEV_DATA));
        if( data == null ){
        	Log.w(TAG, "Record for " + uid + "was found but had no data");
        }

		return ComModule.deserialize(data);
	}
	
	/*******************
	 * DEV LOG METHODS *
	 *******************/
	
	public long storeDevLog(DeviceUID devUID, EventLogICD logRecord){
		String uid = devUID.toString();

		ContentValues newDeviceVal = new ContentValues();
		newDeviceVal.put(DbConst.DEVLOG_UID, uid);	
		newDeviceVal.put(DbConst.DEVLOG_TIME, logRecord.timeStamp.getBytes());
		newDeviceVal.put(DbConst.DEVLOG_TYPE, logRecord.eventType.getBytes());
		newDeviceVal.put(DbConst.DEVLOG_DATA, logRecord.getStatusSection());
		
		return db.insertWithOnConflict(DbConst.DEVLOG_TABLE, null, newDeviceVal, SQLiteDatabase.CONFLICT_REPLACE );
	}
	
	public ArrayList<EventLogICD> getDevLogRecords(DeviceUID devUID){
		//TODO limit number of records returned
		ArrayList<EventLogICD> eventLog = new ArrayList<EventLogICD>(100);
		if( devUID == null )
			return eventLog;

		Cursor c = db.query(DbConst.DEVLOG_TABLE, 
				null, // all columns
				DbConst.DEVLOG_UID + " = ?", 
				new String[]{devUID.toString()}, 
				null, 
				null,
				null);

		c.moveToFirst();
        while (c.isAfterLast() == false) {
        	EventLogICD log = createDevLogFromCursor(c);
        	if(log == null)
        		Log.e(TAG, "Unable to retrieve Dev log record from database");
        	else
        		eventLog.add(log);
            c.moveToNext();
        }
        c.close();
		
		return eventLog;
	}
	
	public int getDevLogRecordCount(DeviceUID devUID){
		String sqlQuery = "SELECT COUNT(*) FROM DevEventLog WHERE UID = ?";
		Cursor c = db.rawQuery(
				sqlQuery, 
				new String[]{devUID.toString()});
		c.moveToFirst();
		int count = c.getInt(0);
        c.close();
		return count;
	}
	
	public int deleteDevLogRecords(DeviceUID devUID){
		String uid = devUID.toString();
		return db.delete(DbConst.DEVLOG_TABLE, DbConst.DEVLOG_UID + " = ?",  new String[]{uid});
	}
	
	private EventLogICD createDevLogFromCursor(Cursor c){
		
		String uidStr = c.getString(c.getColumnIndex(DbConst.DEVLOG_UID));
		byte[] timeRaw = c.getBlob(c.getColumnIndex(DbConst.DEVLOG_TIME));
		IcdTimestamp time = new IcdTimestamp(timeRaw);
		byte typeVal = (byte) c.getInt(c.getColumnIndex(DbConst.DEVLOG_TYPE));
        byte[] eventData = c.getBlob(c.getColumnIndex(DbConst.DEVLOG_DATA));

        DeviceUID devUID = new DeviceUID(uidStr);
        ComModule device = getDevice(devUID);
        
        if( device == null  || eventData == null ){
        	Log.w(TAG, "Record for " + uidStr + "was found but had no data");
        	return null;
        }
        
        if( device.devType == DeviceType.ECOC || device.devType == DeviceType.ECM0){
        	return new EventLogECM(time,typeVal,eventData);
        }else if( device.devType == DeviceType.CSD || device.devType == DeviceType.ACSD){
        	return new EventLogCSD(time,typeVal,eventData);
        }else{
        	return null;
        }
	}
	
	/*******************
	 * HNAD LOG METHODS*
	 *******************/
	//auto timestamp, logNo
	// store user,  ICDMSG sent, ICDMSG received (event type cmd)
	// store user,  eventType (boot, shutoff, getkey)
	
	public long storeHnadLog(NadEventLogType type, String user, IcdMsg sent, IcdMsg received){
		ContentValues newDeviceVal = new ContentValues();
		newDeviceVal.put(DbConst.HNADLOG_TYPE, type.getBytes());	
		newDeviceVal.put(DbConst.HNADLOG_TIME, IcdTimestamp.now().getBytes());
		newDeviceVal.put(DbConst.HNADLOG_USER, user);
		if( sent != null)
			newDeviceVal.put(DbConst.HNADLOG_MSG_SENT, sent.getBytes());
		if( received != null)
			newDeviceVal.put(DbConst.HNADLOG_MSG_RESPONSE, received.getBytes());
		return db.insertWithOnConflict(DbConst.HNADLOG_TABLE, null, newDeviceVal, SQLiteDatabase.CONFLICT_REPLACE );
	}
	
	public long storeHnadLog(NadEventLogType type, String user){
		return storeHnadLog(type,user,null,null);
	}
	
	public ArrayList<HnadEventLog> getHnadLogRecords(){
		//TODO limit number of records returned
		ArrayList<HnadEventLog> eventLog = new ArrayList<HnadEventLog>(10);

		Cursor c = db.query(DbConst.HNADLOG_TABLE, 
				null, // all columns
				null, 
				null, 
				null, 
				null,
				null);

		c.moveToFirst();
        while (c.isAfterLast() == false) {
        	HnadEventLog log = createHnadLogFromCursor(c);
        	if(log == null)
        		Log.e(TAG, "Unable to retrieve Dev log record from database");
        	else
        		eventLog.add(log);
            c.moveToNext();
        }
        c.close();
		
		return eventLog;
	}
	
private HnadEventLog createHnadLogFromCursor(Cursor c){
		int logID = c.getInt(c.getColumnIndex(DbConst.HNADLOG_ID));
		String username = c.getString(c.getColumnIndex(DbConst.HNADLOG_USER));
		IcdTimestamp time = new IcdTimestamp(c.getBlob(c.getColumnIndex(DbConst.HNADLOG_TIME)));
		NadEventLogType eventType = NadEventLogType.fromValue((byte)c.getInt(c.getColumnIndex(DbConst.HNADLOG_TYPE)));
		IcdMsg msgSent = IcdMsg.fromBytes(c.getBlob(c.getColumnIndex(DbConst.HNADLOG_MSG_SENT)));
		IcdMsg msgReceived = IcdMsg.fromBytes(c.getBlob(c.getColumnIndex(DbConst.HNADLOG_MSG_RESPONSE)));
        return new HnadEventLog(logID,username,time,eventType,msgSent,msgReceived);
	}

	public ArrayList<HnadEventLog> getHNADLogRecords(DeviceUID devUID){
		//TODO limit number of records returned
		ArrayList<HnadEventLog> eventLog = new ArrayList<HnadEventLog>(10);
		if( devUID == null )
			return eventLog;
	
		Cursor c = db.query(DbConst.HNADLOG_TABLE, 
				null, // all columns
				DbConst.DEVLOG_UID + " = ?", 
				null, 
				null, 
				null,
				null);
	
		c.moveToFirst();
	    while (c.isAfterLast() == false) {
	    	HnadEventLog log = createHnadLogFromCursor(c);
	    	if(log == null)
	    		Log.e(TAG, "Unable to retrieve Dev log record from database");
	    	else
	    		eventLog.add(log);
	        c.moveToNext();
	    }
	    c.close();
		
	    return eventLog;
	}	
}
