package cste.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBhelper extends SQLiteOpenHelper {
	private static String TAG = "DBAdapter";
	private static final String CREATE_DEVICES_TABLE = 
		"create table " + DbConst.DEVICE_TABLE + " ( " + 
		DbConst.DEV_KEY_ID + " text primary key, " + 
		DbConst.DEV_DATA + " blob not null" + 
		" ) ;"; 
	
	private static final String CREATE_DEVLOG_TABLE = 
		"create table " + DbConst.DEVLOG_TABLE + " ( " + 
		DbConst.DEVLOG_UID + " text, " + 
		DbConst.DEVLOG_TIME + " blob," +
		DbConst.DEVLOG_TYPE + " TINYINT not null," +
		DbConst.DEVLOG_DATA + " blob not null," + 
		" PRIMARY KEY ( " + DbConst.DEVLOG_UID + " , " + DbConst.DEVLOG_TIME + ")" + 
		" ) ;"; 
	
	private static final String CREATE_HNADLOG_TABLE = 
		"create table " + DbConst.HNADLOG_TABLE + " ( " + 
		DbConst.HNADLOG_ID 	 + " INTEGER primary key, " + 
		DbConst.HNADLOG_TIME + " blob not null," +
		DbConst.HNADLOG_USER + " text not null," +
		DbConst.HNADLOG_TYPE + " TINYINT not null," +
		DbConst.HNADLOG_DATA + " blob," +
		DbConst.HNADLOG_RESPONSE + " blob" + 
		" ) ;"; 

	public DBhelper(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, "creating all tables");
		try {
			db.execSQL(CREATE_DEVICES_TABLE);
			db.execSQL(CREATE_DEVLOG_TABLE);
			db.execSQL(CREATE_HNADLOG_TABLE);
		} catch (SQLiteException ex) {
			Log.v("create table exception", ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading dabase");
		db.execSQL("drop table if exists " + DbConst.DEVICE_TABLE);
		db.execSQL("drop table if exists " + DbConst.DEVLOG_TABLE);
		db.execSQL("drop table if exists " + DbConst.HNADLOG_TABLE);
		onCreate(db);
	}
}
