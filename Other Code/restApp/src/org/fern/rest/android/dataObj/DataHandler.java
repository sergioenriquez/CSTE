package org.fern.rest.android.dataObj;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fern.rest.android.cache.DBhandler;
import org.fern.rest.android.dataObj.DataEventReceiver.CommandResult;
import org.fern.rest.android.task.Task;
import org.fern.rest.android.user.User;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

/**
 * Handles requests to retrieve or store user and task data.
 * @author user
 *
 */
public class DataHandler {
    private DBhandler mDatabaseHandler;
    private WebClient mWebClient;
    private DataEventReceiver mCallback;
    private Context mContext;

    public List<String> getAllTags(){
    	return mDatabaseHandler.getTagsByTaskURI(null);
    }

    public DataHandler(Context context, DataEventReceiver der) {
        this.mContext = context;
        this.mCallback = der;
        mDatabaseHandler = new DBhandler(context);
        mDatabaseHandler.open();
        mWebClient = new WebClient(context);
    }

    /**
     * 
     * @param userURI
     * @param username
     * @param password
     * @param progress
     */
    public void authenticateUser(URI userURI, String username, String password) {
        new AsyncAuthenticate(userURI, username, password).execute();
    }

    /**
     * To be added once the server supports this functionality
     * @param user
     * Object with desired username and password for the new account
     */
    public void createUser(User user) {
        
    }

    /**
     * Stores a user record in the local database. This should be called after this user was
     * authenticated succesfully with the server
     * @param user
     * Object with the username, password, and URI for the account
     */
    public void storeUser(User user) {
        mDatabaseHandler.addUser(user);
    }

    /**
     * To be added once the server supports this functionality
     * This will remove the user record from the database
     * @param user
     */
    public void removeUser(User user) {
        boolean postSuccessful = true;
        if (!postSuccessful) {
            mCallback.onRemoveUser(null, CommandResult.NETWORK_ERROR);
            return;
        }

        int recordsRemoved = mDatabaseHandler.removeUserByUri(user.getURI());
        if (mCallback != null) {
            if (recordsRemoved == 0) mCallback.onRemoveUser(null,
                    CommandResult.DATABASE_ERROR);
            else mCallback.onRemoveUser(user, CommandResult.SUCCESS);
        }
    }

    /**
     * Gets the user data from the local database
     * @param uri
     * Unique URI identifiying the user
     * @return
     * User object
     */
    public User getUserByUri(URI uri) {
        return mDatabaseHandler.getUserByURI(uri);
    }

    /**
     * Retrieves a list of all the users on the local database
     * @return
     * List of user objects
     */
    public List<User> getUserList() {
        List<User> userList = mDatabaseHandler.getAllUsers();
        return userList;
    }
    
    /**
     * Sends a request to the web server to refresh this task data
     * @param user
     * @param task
     */
    public void refreshTask(User user, Task task){
    	new RefreshTask(user,task).execute();
    }

    /**
     * This non-blocking method will create/edit a task on the web server, and if succesul it will also create/edit the
     * task record on the local database. Upon completion it will execute the onAddTask callback with the result. 
     * @param user
     * Object containing the authentication credentials
     * @param task
     * Object containing the task details to be saved
     */
    public void saveTaskForUser(User user, Task task) {
    	new SaveTask(user, task).execute();
    }

    /**
     * This non-blocking method will delete a task on the web server, and if succesul it will also delete the
     * task record on the local database. Upon completion it will execute the onRemoveTask callback with the result.
     * @param user
     * Object containing the authentication credentials
     * @param task
     * Object containing the URI of the task to be deleted
     */
    public void deleteTaskForUser(User user, Task task){
    	new DeleteTask(user,task).execute();
    }

    /**
     * Gets all tasks belonging to the given user
     * @param user
     * @param tagList
     * List of strings with tag names
     */
    public void getTaskListByUser(User user, List<String> tagList) {
        mCallback.onReceiveTaskList( mDatabaseHandler.getTasksByUser(user.getURI(), tagList) , CommandResult.SUCCESS);
        new AsyncTaskList(mContext, tagList).execute(user);
    }

    /**
     * Gets all the tags associated with the given task
     * @param task
     * Task
     * @return
     * List of strings with tag names
     */
    public List<String> getTagsForTask(Task task) {
        return mDatabaseHandler.getTagsByTaskURI(task.getURI());
    }
    
    /**
     * Gets a list of tasks marked as being children of the given task
     * @param task
     * @return
     */
    public List<Task> getChildTasks(Task task) {
        return mDatabaseHandler.getChildListByTaskURI(task.getURI());
    }

    /**
     * Gets a list of tasks marked as being the parent of the given task
     * @param task
     * @return
     */
    public List<Task> getParentTasks(Task task) {
        return mDatabaseHandler.getParentListByTaskURI(task.getURI());
    }
    
    private static abstract class AsyncHandler<T, U, V> extends AsyncTask<T, U, V> {
        private SharedPreferences mPreferences;

        protected AsyncHandler(Context context) {
            this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        protected boolean isEtagExpired(Date when) {
            return when == null || isEtagExpired(when.getTime());
        }

        protected boolean isEtagExpired(long when) {
            long difference = System.currentTimeMillis() - when;
            long maxDifference = Long.parseLong(mPreferences.getString(
                    "cachetime", "15")) * 60 * 1000;
            return difference > maxDifference;
        }
    }

    private class AsyncTaskList extends AsyncHandler<User, Integer, List<Task>> {
        private List<String> mTagFilters;
        private CommandResult mCommandResult;
        private boolean forceRefresh = false;
        
        protected AsyncTaskList(Context context, List<String> tagFilters) {
            super(context);
            this.mTagFilters = tagFilters;
        }

        @Override
        protected List<Task> doInBackground(User... users) {
            User user = users[0];
            if ( user.getEtag() == null )
            	forceRefresh = true;
            
            List<Task> taskList;
            List<Task> returnList = new ArrayList<Task>();
            if (!this.isEtagExpired(user.getLastEtagUpdate())) {
                taskList = mDatabaseHandler.getTasksByUser(user.getURI(), mTagFilters);
            } else {
                taskList = mWebClient.getTaskListForUser(user);
                if ( taskList == null){
                	mCommandResult = mWebClient.getLastError();
                	return null;
                }
                else{
                	user.setEtag(mWebClient.getLastEtag());
                    user.setEtagUpdateTime(new Date());
                    storeUser(user);
                }
            }
            
            if ( forceRefresh && taskList != null){
            	List<Task> dbTaskList = mDatabaseHandler.getTasksByUser(user.getURI(), null);
            	for (Task t:dbTaskList)
            		mDatabaseHandler.deleteTaskByURI(t.getURI());
            }
            	
            for (Task task: taskList) {
                URI uri = task.getURI();
                task = mDatabaseHandler.getTaskByURI(uri);
                if (task == null || !this.isEtagExpired(task.getLastEtagUpdate())) {
                    Task webTask = mWebClient.getTaskForUser(user, uri);
                    // if network connection was succesful, replace the database record
                    if ( webTask != null){
                    	webTask.setEtag(mWebClient.getLastEtag());
                    	webTask.setEtagUpdateTime(new Date());
                    	
                    	if ( task == null)
                    		mDatabaseHandler.addTask(user, webTask);
                    	else
                    		mDatabaseHandler.editTaskByURI(webTask);
                    	task = webTask;
                    }
                }
                
                if (task.containsAnyTag(mTagFilters)) {
                    returnList.add(task);
                }
            } 
            mCommandResult = CommandResult.SUCCESS;
            return returnList;
        }

        @Override
        protected void onPostExecute(List<Task> result) {
            super.onPostExecute(result);
            mCallback.onReceiveTaskList(result, mCommandResult);
        }
    }
    
    //TODO
    private class RefreshTask extends AsyncTask<Void, Void, CommandResult> {
        User mUser;
        Task mTask;

        protected RefreshTask(User user, Task task) {
            this.mUser = user;
            this.mTask = task;
        }

        @Override
        protected CommandResult doInBackground(Void... arg0) {
        	Task newTask = mWebClient.getTaskForUser(mUser, mTask.getURI());
            if ( newTask != null ){
                mTask = newTask;
                mDatabaseHandler.addTask(mUser, mTask);
                return CommandResult.SUCCESS;
            }else
            	return mWebClient.getLastError();
        }
        
        @Override
        protected void onPostExecute(CommandResult result){
        	mCallback.onReceiveTask(mTask, result);
        }
    }
   
    private class SaveTask extends AsyncTask<Void, Void, CommandResult> {
        User mUser;
        Task mTask;

        protected SaveTask(User user, Task task) {
            this.mUser = user;
            this.mTask = task;
        }

        @Override
        protected CommandResult doInBackground(Void... arg0) {
        	CommandResult result;
            if ( !mWebClient.saveTaskForUser(mUser, mTask) ){
            	result = mWebClient.getLastError();
            }else
            	result = CommandResult.SUCCESS;
            mDatabaseHandler.addTask(mUser, mTask);
            return result;
        }
        
        @Override
        protected void onPostExecute(CommandResult result){
        	mCallback.onAddTask(mTask, result);
        }
    }
    
    private class DeleteTask extends AsyncTask<Void, Void, CommandResult> {
        User mUser;
        Task mTask;
        
        protected DeleteTask(User user, Task task) {
            this.mUser = user;
            this.mTask = task;
        }

        @Override
        protected CommandResult doInBackground(Void... arg0) {
            if ( mWebClient.deleteTaskForUser(mUser, mTask) ){
                mDatabaseHandler.deleteTaskByURI(mTask.getURI());
            }else
            	return mWebClient.getLastError();
            return CommandResult.SUCCESS;
        }
        
        @Override
        protected void onPostExecute(CommandResult result){
        	mCallback.onRemoveTask(mTask, result);
        }
    }
    
    private class AsyncAuthenticate extends AsyncTask<Void, Void, User> {
        private URI mUrl;
        private String mUsername;
        private String mPassword;
        
        protected AsyncAuthenticate(URI url, String username, String password) {
            mUrl = url;
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected User doInBackground(Void... params) {
            boolean web_success = mWebClient.authenticateUser(mUrl, mUsername, mPassword);
            if (!web_success) {
                return null;
            } else {
                // Will overwrite user upon existing URI
                User user = new User(mUsername, mPassword, mUrl);
                mDatabaseHandler.addUser( user );
                return user;
            }
        }
        
        @Override
        protected void onPostExecute(User result) {
            super.onPostExecute(result);
            mCallback.onAddUser(result, result != null ? CommandResult.SUCCESS : mWebClient.getLastError());
        }
    }
}
