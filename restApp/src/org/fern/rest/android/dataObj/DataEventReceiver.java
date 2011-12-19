package org.fern.rest.android.dataObj;

import org.fern.rest.android.task.Task;
import org.fern.rest.android.user.User;

import java.util.List;

/**
 * Implemented by activites that need to be notified that an async data request was completed
 * @author user
 */
public class DataEventReceiver {
	
	/**
	 * Identifies the possible error conditions when requesting data from a web server
	 */
	public static enum CommandResult{
		SUCCESS,
		NETWORK_ERROR,
		DATABASE_ERROR,
		DEPENDENCY_ERROR,
		AUTHENTICATION,
		HOSTNOTFOUND,
		RESOURCENOTFOUND,
		BADREQUEST,
		PRECONDITIONFAILED,
		OTHER
	}
	
	public void onAddUser(User user, CommandResult cr) {}
	public void onRemoveUser(User user, CommandResult cr) {}
	public void onAddTask(Task task, CommandResult cr) {}
	public void onReceiveTaskList(List<Task> tasks, CommandResult cr) {}
	public void onRemoveTask(Task task, CommandResult cr) {}
	public void onAddTag(String tag, CommandResult cr) {}
	public void onremoveTag(String tag, CommandResult cr) {}
	public void onReceiveTask(Task task, CommandResult cr){}
}
