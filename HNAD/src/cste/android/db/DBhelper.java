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
		DbConst.DEV_DATA + " blob " + 
		" ) ;"; 

	public DBhelper(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, "creating all tables");
		try {
			db.execSQL(CREATE_DEVICES_TABLE);
		} catch (SQLiteException ex) {
			Log.v("create table exception", ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading dabase");
		db.execSQL("drop table if exists " + DbConst.DEVICE_TABLE);
		onCreate(db);
	}
}
