package org.fern.rest.android.task;

import java.util.ArrayList;
import java.util.List;

import org.fern.rest.android.R;
import org.fern.rest.android.dataObj.DataEventReceiver;
import org.fern.rest.android.dataObj.DataHandler;
import org.fern.rest.android.user.AccountsActivity;
import org.fern.rest.android.user.User;
import org.fern.rest.fling.FlingHandler;
import org.fern.rest.fling.MyGestureDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

// TODO use user class to pull tasks
@SuppressWarnings("unused")
public class TasksActivity extends ListActivity implements OnTouchListener, FlingHandler {
    private TaskAdapter mAdapter;
    private User mUser;
    private ProgressDialog mDialog;
    private DataHandler mHandler;
    private List<Task> storedTaskList = null;
    private final int SAVE_ACTIVITY_CODE = 1;
    private final int DELETE_ACTIVITY_CODE = 2;
    private final int FILTER_ACTIVITY_CODE = 3;
    private final int VIEW_ACTIVITY_CODE = 4;
    private final int FILTER_DIALOG = 5;
    private boolean[] tagChecked = null;
    private int totalTags = 0;
    private List<String> tagFilterList = null;
    private int mLastItemPosition = -1;
    private GestureDetector gestureDetector;

    @Override
    public Object onRetainNonConfigurationInstance() {
        return storedTaskList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIEW_ACTIVITY_CODE) {
            mHandler.getTaskListByUser(mUser, null);
        }

        if (resultCode == RESULT_OK) {
            Task t = (Task) data.getParcelableExtra("task");
            if (t != null) {
                if (requestCode == SAVE_ACTIVITY_CODE) mHandler.getTaskListByUser(mUser, null);
                else if (requestCode == DELETE_ACTIVITY_CODE) mHandler.deleteTaskForUser(mUser, t);
            }
        }
    }
    
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mUser = (User) intent.getParcelableExtra("user");
        mAdapter = new TaskAdapter(this, R.layout.task_item);
        setListAdapter(mAdapter);

        mHandler = new DataHandler(this, new DataEventReceiver() {
            @Override
            public void onReceiveTaskList(List<Task> tasks, CommandResult cr) {
                super.onReceiveTaskList(tasks, cr);
                setProgressBarIndeterminateVisibility(false);
                
                if ( tasks != null){
                	storedTaskList = tasks;
                    mAdapter.clear();
                    mAdapter.addAll(tasks);
                }
                
                if (cr != CommandResult.SUCCESS) {
                    switch (cr) {
                        case RESOURCENOTFOUND:
                            Toast.makeText(getApplicationContext(),
                                            "User URI does not exist on server", Toast.LENGTH_SHORT)
                                            .show();
                            break;
                        case AUTHENTICATION:
                            Toast.makeText(getApplicationContext(), "Invalid username or password",
                                            Toast.LENGTH_SHORT).show();
                            break;
                        case NETWORK_ERROR:
                        case HOSTNOTFOUND:
                            Toast.makeText(getApplicationContext(),
                                            "Unable to connect to the web server",
                                            Toast.LENGTH_SHORT).show();
                            break;
                        case OTHER:
                            Toast.makeText(getApplicationContext(), "Unknown error",
                                            Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            public void onRemoveTask(Task task, CommandResult cr) {
                super.onRemoveTask(task, cr);
                if (cr == CommandResult.SUCCESS) {
                    mAdapter.remove(task);
                    mHandler.getTaskListByUser(mUser, null);
                } else {
                    switch (cr) {
                        case RESOURCENOTFOUND:
                            Toast.makeText(getApplicationContext(),
                                            "User URI does not exist on server", Toast.LENGTH_SHORT)
                                            .show();
                            break;
                        case AUTHENTICATION:
                            Toast.makeText(getApplicationContext(), "Invalid username or password",
                                            Toast.LENGTH_SHORT).show();
                            break;
                        case NETWORK_ERROR:
                        case HOSTNOTFOUND:
                            Toast.makeText(getApplicationContext(),
                                            "Unable to connect to the web server",
                                            Toast.LENGTH_SHORT).show();
                            break;
                        case OTHER:
                            Toast.makeText(getApplicationContext(), "Unknown error",
                                            Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });

        // retrieve the stored task list from memmory if avalible
        storedTaskList = (List<Task>) getLastNonConfigurationInstance();
        if ( storedTaskList == null){
        	mHandler.getTaskListByUser(mUser, null);
        	setProgressBarIndeterminateVisibility(true);
        }
        else
        	mAdapter.addAll(storedTaskList);

        ListView lv = getListView();
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	mLastItemPosition = position;
                Task t = mAdapter.getItem(mLastItemPosition);
                Intent taskIntent = new Intent(TasksActivity.this, DescriptionActivity.class);
                taskIntent.putExtra("task", t);
                taskIntent.putExtra("user", mUser);
                startActivityForResult(taskIntent, VIEW_ACTIVITY_CODE);
            }
        });
        
        if (storedTaskList.size() > 0)
        	mLastItemPosition = 0;

        getListView().setOnTouchListener(this);
        gestureDetector = new GestureDetector(new MyGestureDetector(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case FILTER_DIALOG:
                List<String> tagList = mHandler.getAllTags();
                CharSequence[] items = new CharSequence[tagList.size()];
                for (int i = 0; i < tagList.size(); i++)
                    items[i] = tagList.get(i);

                if (tagChecked == null || totalTags != tagList.size()) {
                    tagChecked = new boolean[tagList.size()];
                    totalTags = tagList.size();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        List<String> tagList = mHandler.getAllTags();
                        tagFilterList = new ArrayList<String>(totalTags);
                        for (int i = 0; i < totalTags; i++) {
                            if (tagChecked[i]) tagFilterList.add(tagList.get(i));
                        }
                        mHandler.getTaskListByUser(mUser, tagFilterList);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                builder.setMultiChoiceItems(items, tagChecked,
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        tagChecked[which] = isChecked;
                                    }
                                });
                dialog = builder.create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        removeDialog(FILTER_DIALOG);
                    }
                });

                break;
        }
        return dialog;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.tasksMenuAdd:
                intent = new Intent(this, TaskEditActivity.class);
                intent.putExtra("user", mUser);
                startActivityForResult(intent, SAVE_ACTIVITY_CODE);
                break;
            case R.id.tasksMenuRefresh:
                // force web query by reseting ETAG
                mUser.setEtag(null);
                mUser.setEtagUpdateTime(null);
                mHandler.getTaskListByUser(mUser, tagFilterList);
                break;
            case R.id.tasksMenuFilterTags:
                showDialog(FILTER_DIALOG);
                break;
            case R.id.taskSortName:
            	mAdapter.sortList(TaskAdapter.NAME);
            	break;
            case R.id.taskSortPriority:
            	mAdapter.sortList(TaskAdapter.PRIORITY);
            	break;
            case R.id.taskSortProgress:
            	mAdapter.sortList(TaskAdapter.PROGRESS);
            	break;
            case R.id.taskSortActivationDate:
            	mAdapter.sortList(TaskAdapter.START_DATE);
            	break;
            case R.id.taskSortAdditionDate:
            	mAdapter.sortList(TaskAdapter.ADDED_DATE);
            	break;
            case R.id.taskSortCompletionTime:
            	mAdapter.sortList(TaskAdapter.ESTIMATED_COMPLETION_TIME);
            	break;
            case R.id.taskSortExpirationDate:
            	mAdapter.sortList(TaskAdapter.EXPIRATION_DATE);
            	break;
            case R.id.taskSortModificationDate:
            	mAdapter.sortList(TaskAdapter.LAST_MODIFIED_DATE);
            	break;
            case R.id.tasksMenuAccounts:
                intent = new Intent(this, AccountsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_activity_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        String toast;
        Task t = mAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.tasksActivityContextMenuEdit:
                Intent intent = new Intent(this, TaskEditActivity.class);
                intent.putExtra("user", mUser);
                intent.putExtra("task", t);
                try {
                	startActivityForResult(intent,SAVE_ACTIVITY_CODE);
                } catch (RuntimeException re) {
                    Log.wtf("", "");
                }
                return true;
            case R.id.tasksActivityContextMenuDelete:
                //TODO add progress bar
                this.mHandler.deleteTaskForUser(mUser, t);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void OnFlingLeft() {
		finish();
	}

	@Override
	public void OnFlingRight() {
		if ( mLastItemPosition != -1){
			Task t = mAdapter.getItem(mLastItemPosition);
			Intent taskIntent = new Intent(TasksActivity.this, DescriptionActivity.class);
			taskIntent.putExtra("task", t);
			taskIntent.putExtra("user", mUser);
			startActivityForResult(taskIntent, VIEW_ACTIVITY_CODE);
		}
	}
}
