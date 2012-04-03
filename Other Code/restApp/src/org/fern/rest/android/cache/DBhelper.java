package org.fern.rest.android.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBhelper extends SQLiteOpenHelper {
	private static final String CREATE_USER_TABLE = 
		"create table " + DBConst.USER_TABLE_NAME + " ( " + 
		UserConst.KEY_ID + " integer primary key autoincrement , " + 
		UserConst.NAME + " text not null, " + 
		UserConst.PASSWORD + " text not null, " + 
		UserConst.ETAG + " text, " + 
		UserConst.ETAG_UPDATE + " integer, " + 
		UserConst.URI + " text unique not null ON CONFLICT REPLACE" + " ) ;"; // on UPDATE CASCADE on DELETE CASCADE ON CONFLICT REPLACE
	
	private static final String CREATE_TAGS_TABLE = 
		"create table " + DBConst.TAG_TABLE_NAME + " ( " + 
		TagConst.KEY_ID + " integer primary key autoincrement, " + 
		TagConst.NAME + " text not null " + ");";
	
	private static final String CREATE_TASKS_TABLE = 
		"create table " + DBConst.TASK_TABLE_NAME + " ( " + 
		TaskConst.KEY_ID + " integer primary key autoincrement, " + 
		TaskConst.USER_ID + " integer not null, " +
		TaskConst.NAME + " text not null, " + 
		TaskConst.DETAIL + " text not null, " + 
		TaskConst.URI + " text unique not null, " + 
		TaskConst.TYPE + " text, " + 
		TaskConst.STATUS + " text, " + 
		TaskConst.PRIORITY + " integer, " + 
		TaskConst.PROGRESS + " integer, " + 
		TaskConst.PROCESS_PROGRESS + " integer, " + 
		TaskConst.ACTIVATION_TIME + " integer, " + 
		TaskConst.MODIFICATION_TIME + " integer, " + 
		TaskConst.ADDITION_TIME + " integer, " + 
		TaskConst.EXPIRATION_TIME + " integer, " + 
		TaskConst.ESTIMATED_COMPLETION_TIME + " text, " + 
		TaskConst.ETAG + " text, " + 
		TaskConst.LAST_UPDATED + " integer not null " + ");";
	
//	private static final String CREATE_USER_TASKS_TABLE = 
//		"create table " + DBConst.USERTASK_TABLE_NAME + " ( " +
//		UserTaskConst.KEY_ID + " integer primary key autoincrement, " + 
//		UserTaskConst.USER_ID + " integer, " + 
//		UserTaskConst.TASK_ID + " integer " + ");"; 
	
	private static final String CREATE_TASK_TAGS_TABLE = 
		"create table " + DBConst.TASKTAG_TABLE_NAME + " ( " +
		TaskTagsConst.KEY_ID + " integer primary key autoincrement, " + 
		TaskTagsConst.TAG_ID + " integer, " + 
		TaskTagsConst.TASK_ID + " integer " + 
		" REFERENCES "+ DBConst.TASK_TABLE_NAME + "(" + TaskConst.KEY_ID + ") ON DELETE CASCADE " + ");"; 
	
//	private static final String CREATE_TASK_TASK_TABLE = 
//		"create table " + DBConst.TASKTASK_TABLE_NAME + " ( " +
//		TaskTaskConst.P_TASK_ID + " integer, " + 
//		TaskTaskConst.C_TASK_ID + " integer " + ");"; 

	public DBhelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v("DB helper on create", "creating all tables");
		try {
			db.execSQL(CREATE_USER_TABLE);
			db.execSQL(CREATE_TAGS_TABLE);
			db.execSQL(CREATE_TASKS_TABLE);
			//db.execSQL(CREATE_USER_TASKS_TABLE);
			db.execSQL(CREATE_TASK_TAGS_TABLE);
			//db.execSQL(CREATE_TASK_TASK_TABLE);
		} catch (SQLiteException ex) {
			Log.v("create table exception", ex.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("TaskDBAdapter", "Upgrading dabase");
		db.execSQL("drop table if exists " + DBConst.USER_TABLE_NAME);
		db.execSQL("drop table if exists " + DBConst.TAG_TABLE_NAME);
		db.execSQL("drop table if exists " + DBConst.TASK_TABLE_NAME);
		db.execSQL("drop table if exists " + DBConst.TASKTAG_TABLE_NAME);
		db.execSQL("drop table if exists " + DBConst.USERTASK_TABLE_NAME);
		db.execSQL("drop table if exists " + DBConst.TASKTAG_TABLE_NAME);
		onCreate(db);
	}
}