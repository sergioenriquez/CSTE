package org.fern.rest.android.cache;

import org.fern.rest.android.Duration;
import org.fern.rest.android.RestApplication.Priority;
import org.fern.rest.android.task.Task;
import org.fern.rest.android.user.User;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class DBhandler {
	private SQLiteDatabase db;
	private final Context context;
	private final DBhelper dbHelper;

	public DBhandler(Context c){
		context = c;
		dbHelper = new DBhelper(context,DBConst.DB_NAME,null,DBConst.DB_VERSION);
	}
	
	/**
	 * Closes the database file
	 */
	public void close(){
		db.close();
	}

	/**
	 * Creates and initializes the database file
	 * @throws SQLiteException
	 */
	public void open() throws SQLiteException{
		try{
			db = dbHelper.getWritableDatabase();
		}catch(SQLiteException ex){
			Log.e("open database exception", ex.getMessage());
			db = dbHelper.getWritableDatabase();
		}
	}
	
	/**
     * Retrieves a task dataset by associated user and tag
     * @param userUri
     * selects task records associated with this user uri
     * @param tagUri
     * (optional) filter resulting set to only include tasks associated with this list of tags
     * @return
     * Cursor for a task dataset containing rows matching this request
     */
    public List<Task> getTasksByUser(URI userUri, List<String> tagList){
        String []args;
        long userID = this.getUserID(userUri);

        String SELECT_STATEMENT = "SELECT distinct "+
        "tasks.taskID,tasks.taskName,tasks.details,tasks.uri,tasks.type,tasks.status,tasks.priority,tasks.progress,tasks.processProgress,tasks.activationTime,tasks.modificationTime,tasks.additionTime,tasks.expirationTime,tasks.estimatedCompletionTime,tasks.lastUpdated,tasks.etag "+
        "FROM tasks " +
        "left outer join tasktags on tasktags.taskID = tasks.taskID "+
        "left outer join tags on tags.tagID = tasktags.tagID ";

        String WHERE_CLAUSE = "where userID = ?";
        args = new String[]{ Long.toString(userID) };

        if ( tagList != null && tagList.size() > 0){
            String tags = TextUtils.join("','", tagList);
            WHERE_CLAUSE += " and tags.tagName in ( '"+tags+"' )";
        }

        Cursor c = db.rawQuery(SELECT_STATEMENT+WHERE_CLAUSE,args );  
        c.moveToFirst();
        List<Task> taskList = new ArrayList<Task>(c.getCount());
        while (c.isAfterLast() == false) {
            Task t = createTaskFromCursor(c);
            taskList.add(t);
            c.moveToNext();
        }
        c.close();
        return taskList;
    }
	
	/*
	 * USER TABLE FUNCTIONS
	 */
	

    /**
     * Stores the user data on the local database, or modifies the record if it already exists
     */
	public long addUser(User user){
		return addUser(user.getName(), user.getPassword(), user.getURI(), user.getEtag(), user.getLastEtagUpdate());
	}
	
	/**
	 * Creates a user record in the database, or replaces the username and password if a URI already exists for it
	 * @param object2 
	 * @param object 
	 */
	private long addUser(String userName, String password, URI uri, String ETag, Date lastEtagUpdate){
		long userID = this.getUserID(uri);
		if ( userID != -1 ){
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(UserConst.NAME, userName);
			newTaskValue.put(UserConst.PASSWORD, password);	
			
			if ( lastEtagUpdate != null){
				newTaskValue.put(UserConst.ETAG, ETag);	
				newTaskValue.put(UserConst.ETAG_UPDATE, lastEtagUpdate.getTime());		
			}
				
			try{
				return db.update(DBConst.USER_TABLE_NAME, newTaskValue, UserConst.KEY_ID + " = ? ", new String[]{Long.toString(userID)});
			}catch(SQLiteException ex){
				Log.e("insert database exception", ex.getMessage());
				return -1;
			}
		}
		
		try{
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(UserConst.NAME, userName);
			newTaskValue.put(UserConst.PASSWORD, password);	
			newTaskValue.put(UserConst.URI, uri.toString());			
			return db.insert(DBConst.USER_TABLE_NAME, null, newTaskValue);
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}
	
	/**
	 * Deletes the specified user record from the database
	 * @param uri
	 * The unique URI for this user record
	 * @return
	 * returns 1 if the operation was successful, 0 otherwise
	 */
	public int removeUserByUri(URI uri){
		return db.delete(DBConst.USER_TABLE_NAME, 
				UserConst.URI + " = ?", 
				new String[]{uri.toString()});
	}
	
	/**
	 * Gets all the user records in the local database
	 * @return
	 * List of User objects
	 */
	public List<User> getAllUsers(){
		Cursor c = db.query(DBConst.USER_TABLE_NAME, null, null, null, null, null, null);
		List<User> userList = new ArrayList<User>(c.getCount());
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            User user = createUserFromCursor(c);
            userList.add(user);
            c.moveToNext();
        }
        c.close();
        return userList;
	}
	
	/**
	 * Gets a user record by its uri
	 * @param uri
	 * @return
	 * User object, null if the record was not found
	 */
	public User getUserByURI(URI uri)
	{
		Cursor c = db.query(DBConst.USER_TABLE_NAME, 
				null, // all columns
				UserConst.URI + " = ?", 
				new String[]{uri.toString()}, 
				null, 
				null,
				null);
		User u = null;
		if (c.moveToFirst())
            u = createUserFromCursor(c);

		c.close();
		return u;
	}
	
	/**
	 * Changes the stored ETAG for the user and updated the last etag update timestamp
	 * @param userURI
	 * @param newEtag
	 * @return
	 * -1 if user record not found
	 */
	public int changeUserEtag(URI userURI, String newEtag){
		long userID = this.getUserID(userURI);
		
		if ( userID == -1)
			return -1;
		
		ContentValues newTaskValue = new ContentValues();
		newTaskValue.put(UserConst.ETAG, newEtag);
		newTaskValue.put(UserConst.ETAG_UPDATE, new Date().getTime());		
		
		try{
			return db.update(DBConst.USER_TABLE_NAME, newTaskValue, UserConst.KEY_ID + " = ? ", new String[]{Long.toString(userID)});
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}
	
	private User createUserFromCursor(Cursor c){
		String name = c.getString(c.getColumnIndex(UserConst.NAME));
        String password = c.getString(c.getColumnIndex(UserConst.PASSWORD));
		String uri = c.getString(c.getColumnIndex(UserConst.URI));
		String etag = c.getString(c.getColumnIndex(UserConst.ETAG));
        Date etagUpdate = null;
        if (!c.isNull(c.getColumnIndex(UserConst.ETAG_UPDATE)) )
        	etagUpdate = new Date(c.getLong(c.getColumnIndex(UserConst.ETAG_UPDATE)));

		return new User(name,password,uri,etag,etagUpdate);
	}

	/* 
	 * TASK TABLE FUNCTIONS
	 */
	
	/**
	 * Modifies the stored task data on the local database
	 */
	public long editTaskByURI(Task t){
		return editTaskByURI(
				t.getURI(),
				t.getName(),
				t.getDetails(),
				t.getType(),
				t.getStatus(),
				t.getPriority(),
				t.getProgress(),
				t.getProcessProgress(),
				t.getActivatedDate(),
				t.getAdditionDate(),
				t.getExpirationDate(),
				t.getEstimatedCompletionTime(),
				t.getModifiedDate());
	}

	private long editTaskByURI(
			URI uri,
			String taskName, 
			String details,
			String type,
			String status,
			Priority priority,
			int progress,
			int process_progress,
			Date activationTime,
			Date additionTime,
			Date expirationTime,
			Duration estimatedCompletion,
			Date modificationTime
			)
	{
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(TaskConst.NAME, taskName);
			newTaskValue.put(TaskConst.DETAIL, details);
			newTaskValue.put(TaskConst.URI, uri.toString());
			newTaskValue.put(TaskConst.TYPE, type);
			newTaskValue.put(TaskConst.STATUS, status);
			newTaskValue.put(TaskConst.PRIORITY, priority.ordinal());
			newTaskValue.put(TaskConst.PROGRESS, progress);
			newTaskValue.put(TaskConst.PROCESS_PROGRESS, process_progress);
			
			if ( activationTime != null)
				newTaskValue.put(TaskConst.ACTIVATION_TIME, activationTime.getTime());
			if ( additionTime != null)
				newTaskValue.put(TaskConst.ADDITION_TIME, additionTime.getTime());
			if ( expirationTime != null)
				newTaskValue.put(TaskConst.EXPIRATION_TIME, expirationTime.getTime());
			if ( estimatedCompletion != null)
				newTaskValue.put(TaskConst.ESTIMATED_COMPLETION_TIME, estimatedCompletion.toString());
			
			if ( modificationTime != null)
				newTaskValue.put(TaskConst.MODIFICATION_TIME, modificationTime.toString());
			
			newTaskValue.put(TaskConst.LAST_UPDATED, new Date().getTime() );
			
		try{
			return db.update(DBConst.TASK_TABLE_NAME, newTaskValue, TaskConst.URI + " = ? ", new String[]{uri.toString()});
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}

	private long addTag(String tagName){
		ContentValues newTaskValue = new ContentValues();
		newTaskValue.put(TagConst.NAME, tagName);
		try{
			return db.insert(DBConst.TAG_TABLE_NAME, null, newTaskValue);
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}
	
	private long getTagID( String tagName ){
		Cursor c = db.query(DBConst.TAG_TABLE_NAME, 
				null, 
				TagConst.NAME + " = ?", 
				new String[]{tagName}, 
				null, 
				null,
				null);
		
		int id = -1;
		if ( c.moveToFirst() )
			id = c.getInt(c.getColumnIndex(TagConst.KEY_ID));
		c.close();
		return id;
	}
	
	private long getTaskID( URI taskUri ){
		Cursor c = db.query(DBConst.TASK_TABLE_NAME, 
				null, 
				TaskConst.URI+ " = ? ", 
				new String[]{taskUri.toString()}, 
				null, 
				null,
				null);
		
		int id = -1;
		if ( c.moveToFirst() )
			id = c.getInt(c.getColumnIndex(TaskConst.KEY_ID));
		c.close();
		return id;
	}
	
	private long getTaskTagID( long taskID, long tagID)
	{
		Cursor c = db.query(DBConst.TASKTAG_TABLE_NAME, 
				null, 
				TaskTagsConst.TASK_ID + " = ? AND " + TaskTagsConst.TAG_ID + " = ? ", 
				new String[]{Long.toString(taskID),Long.toString(tagID)}, 
				null, 
				null,
				null);
		
		int id = -1;
		if ( c.moveToFirst() )
			id = c.getInt(c.getColumnIndex(TaskTagsConst.KEY_ID));
		c.close();
		return id;
	}
	
	private long getUserID( URI userUri)
	{
		Cursor c = db.query(DBConst.USER_TABLE_NAME, 
				null, 
				UserConst.URI + " = ? ", 
				new String[]{userUri.toString()}, 
				null, 
				null,
				null);
		
		int id = -1;
		if ( c.moveToFirst() )
			id = c.getInt(c.getColumnIndex(UserConst.KEY_ID));
		c.close();
		return id;
	}

	
	/**
	 * Associates the given tag to to a task. If the tag is not on the database it will be added to the tag table
	 * @param taskUri
	 * @param tagName
	 * @return
	 */
	public long linkTaskToTag(URI taskUri, String tagName){
		long tagID = getTagID(tagName);
		long taskID = getTaskID(taskUri);
		
		if ( taskID == -1)
			return -1; // error: task does not exist
		
		if ( tagID == -1)
			tagID = addTag(tagName);
		
		long linkID = getTaskTagID(tagID,taskID);
		
		if ( linkID != -1)
			return linkID; // link already exists
		
		ContentValues newTaskValue = new ContentValues();
		newTaskValue.put(TaskTagsConst.TAG_ID, tagID);
		newTaskValue.put(TaskTagsConst.TASK_ID, taskID);
		
		try{
			return db.insert(DBConst.TASKTAG_TABLE_NAME, null, newTaskValue);
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}
	
	/**
	 * creates a new task record, or modifies an existing one if already present
	 * @param u
	 * @param t
	 * @return
	 */
	public long addTask(User u, Task t){
		long taskID = this.getTaskID(t.getURI());
		if ( taskID != -1)
			this.deleteTaskByURI(t.getURI());

		return addTask(
				t.getURI(),
				u.getURI(),
				t.getName(),
				t.getDetails(),
				t.getType(),
				t.getStatus(),
				t.getPriority(),
				t.getProgress(),
				t.getProcessProgress(),
				t.getActivatedDate(),
				t.getAdditionDate(),
				t.getExpirationDate(),
				t.getEstimatedCompletionTime(),
				t.getTags(),
				t.getModifiedDate());
	}
	
	private long addTask(
			URI taskUri,
			URI userUri,
			String taskName, 
			String details,
			String type,
			String status,
			Priority priority,
			int progress,
			int process_progress,
			Date activationTime,
			Date additionTime,
			Date expirationTime,
			Duration estimatedCompletion,
			Set<String> tagList,
			Date modificationDate
			)
	{
			long userID = this.getUserID(userUri);
		
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(TaskConst.NAME, taskName);
			newTaskValue.put(TaskConst.USER_ID, userID);
			newTaskValue.put(TaskConst.DETAIL, details);
			newTaskValue.put(TaskConst.URI, taskUri.toString());
			newTaskValue.put(TaskConst.TYPE, type);
			newTaskValue.put(TaskConst.STATUS, status);
			newTaskValue.put(TaskConst.PRIORITY, priority.ordinal());
			newTaskValue.put(TaskConst.PROGRESS, progress);
			newTaskValue.put(TaskConst.PROCESS_PROGRESS, process_progress);
			
			if ( activationTime != null)
				newTaskValue.put(TaskConst.ACTIVATION_TIME, activationTime.getTime());
			if ( additionTime != null)
				newTaskValue.put(TaskConst.ADDITION_TIME, additionTime.getTime());
			if ( expirationTime != null)
				newTaskValue.put(TaskConst.EXPIRATION_TIME, expirationTime.getTime());
			if ( estimatedCompletion != null)
				newTaskValue.put(TaskConst.ESTIMATED_COMPLETION_TIME, estimatedCompletion.toString());
			if ( modificationDate != null)
				newTaskValue.put(TaskConst.MODIFICATION_TIME, modificationDate.toString());
			
			newTaskValue.put(TaskConst.LAST_UPDATED, new Date().getTime() );
			
		try{
			long items = db.insert(DBConst.TASK_TABLE_NAME, null, newTaskValue);
			for(String tag:tagList)
				linkTaskToTag(taskUri, tag);
			return items;
		}catch(SQLiteException ex){
			Log.e("insert database exception", ex.getMessage());
			return -1;
		}
	}
	
	
	/**
	 * Gets all the tags stored on the local database
	 * @return
	 */
	public List<String> getAllTags()
	{	
		List<String> tagList = new ArrayList<String>();
		String QUERY = "SELECT distinct tags.tagName from tags "+
		"left outer join tasktags on tags.tagID = tasktags.tagID "+
		"left outer join tasks on tasktags.taskID = tasks.taskID ";//+

		Cursor c = db.rawQuery(QUERY,null ); 
		c.moveToFirst();
		while (c.isAfterLast() == false) {
            String tag = c.getString(c.getColumnIndex(TagConst.NAME));
            tagList.add(tag);
            c.moveToNext();
        }
		c.close();
		return tagList;
	}
	
	/**
	 * Gets all the tags associated with a given task
	 * @param taskURI
	 * @return
	 */
	public List<String> getTagsByTaskURI(URI taskURI)
	{
		List<String> tagList = new ArrayList<String>();
		String []args = null;
		String QUERY = "SELECT distinct tags.tagName from tags "+
		"left outer join tasktags on tags.tagID = tasktags.tagID "+
		"left outer join tasks on tasktags.taskID = tasks.taskID ";
		
		if (taskURI !=null ){
			QUERY += "WHERE tasks.uri = ?";
			args = new String[]{ taskURI.toString() };
		}

		Cursor c = db.rawQuery(QUERY,args ); 
		c.moveToFirst();
		while (c.isAfterLast() == false) {
            String tag = c.getString(c.getColumnIndex(TagConst.NAME));
            tagList.add(tag);
            c.moveToNext();
        }
		c.close();
		return tagList;
	}
	
	/**
	 * Gets the stored task details
	 * @param taskURI
	 * @return
	 */
	public Task getTaskByURI(URI taskURI){
		Cursor c = db.query(DBConst.TASK_TABLE_NAME, 
				null, // all columns
				TaskConst.URI + " = ?", 
				new String[]{taskURI.toString()}, 
				null, 
				null,
				null);
		
		c.moveToFirst();
		Task t = null;
		if ( c.isAfterLast() == false )
			t = createTaskFromCursor(c);
		c.close();
		return t;
	}
	
	private Task createTaskFromCursor(Cursor c)
	{
		Task t = new Task();
		t.setKeyID(c.getLong(c.getColumnIndex(TaskConst.KEY_ID)));
		t.setName(c.getString(c.getColumnIndex(TaskConst.NAME)));
		
		URI taskURI = URI.create(c.getString(c.getColumnIndex(TaskConst.URI)));
		t.setURI(taskURI);
		
		t.setDetails(c.getString(c.getColumnIndex(TaskConst.DETAIL)));
		t.setStatus(c.getString(c.getColumnIndex(TaskConst.STATUS)));
		t.setType(c.getString(c.getColumnIndex(TaskConst.TYPE)));
		t.setPriority(c.getInt(c.getColumnIndex(TaskConst.PRIORITY)));
		t.setProgress(c.getInt(c.getColumnIndex(TaskConst.PROGRESS)));
		t.setProcessProgress(c.getInt(c.getColumnIndex(TaskConst.PROCESS_PROGRESS)));
		
		if ( c.isNull(c.getColumnIndex(TaskConst.ADDITION_TIME)))
			t.setAdditionDate(0L);
		else
			t.setAdditionDate(c.getLong(c.getColumnIndex(TaskConst.ADDITION_TIME)));
		
		if ( c.isNull(c.getColumnIndex(TaskConst.MODIFICATION_TIME)))
			t.setModifiedDate(0L);
		else
			t.setModifiedDate(c.getLong(c.getColumnIndex(TaskConst.MODIFICATION_TIME)));
		
		if ( c.isNull(c.getColumnIndex(TaskConst.ACTIVATION_TIME)))
			t.setActivatedDate(0L);
		else
			t.setActivatedDate(c.getLong(c.getColumnIndex(TaskConst.ACTIVATION_TIME)));
		
		if ( c.isNull(c.getColumnIndex(TaskConst.EXPIRATION_TIME)))
			t.setExpirationDate(0L);
		else
			t.setExpirationDate(c.getLong(c.getColumnIndex(TaskConst.EXPIRATION_TIME)));
		

		t.setEstimatedCompletionTime(c.getString(c.getColumnIndex(TaskConst.ESTIMATED_COMPLETION_TIME)));

		t.addAllTags( getTagsByTaskURI(taskURI) );
		//t.addAllChildren(getParentListByTaskURI(taskURI));
		//t.addAllParents(getChildListByTaskURI(taskURI));

		return t;
	}
	
	/**
	 * To be added once server supports this
	 * @param taskUri
	 * @return
	 */
	public List<Task> getParentListByTaskURI(URI taskUri){
		List<Task> listURI = new ArrayList<Task>();
		return listURI;
	}
	
	/**
	 * To be added once server supports this
	 * @param taskUri
	 * @return
	 */
	public List<Task> getChildListByTaskURI(URI taskUri){
		List<Task> listURI = new ArrayList<Task>();
		return listURI;
	}
	
	/**
	 * Removes a given task by its uri and also deletes associeated task-tag entries
	 * @param taskURI
	 * @return
	 */
	public void deleteTaskByURI(URI taskURI){
		long taskID = this.getTaskID(taskURI);
		db.delete(DBConst.TASKTAG_TABLE_NAME, TaskTagsConst.TASK_ID + " = ?", new String[]{Long.toString(taskID)});
		db.delete(DBConst.TASK_TABLE_NAME, TaskConst.URI + " = ?", new String[]{taskURI.toString()});
		db.delete(DBConst.TAG_TABLE_NAME, "(select count() from taskTags where taskTags.tagID = tags.tagID group by taskTags.tagID) is null", null);
	}
}
