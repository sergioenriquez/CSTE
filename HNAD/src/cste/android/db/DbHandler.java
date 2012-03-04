package cste.android.db;

import java.util.Hashtable;
import java.util.List;

import cste.components.ComModule;
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DbHandler {
	private static String TAG = "Db handler";
	private SQLiteDatabase db;
	private final Context context;
	private final DBhelper dbHelper;
	
	/***
	 * 
	 * @param device
	 * @return
	 */
	public long storeDevice(ComModule device){
		String uid = device.UID().toString();
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
	public ComModule getDevice(DeviceUID devUID){
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
	
	public Hashtable<String,ComModule> getStoredDevices(){

		Hashtable<String, ComModule> deviceMap = new Hashtable<String,ComModule>(8);
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
        	else
        		deviceMap.put(cm.UID().toString(), cm);
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
}
