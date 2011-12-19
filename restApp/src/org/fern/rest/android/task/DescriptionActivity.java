package org.fern.rest.android.task;

import org.fern.rest.android.R;
import org.fern.rest.android.RestApplication;
import org.fern.rest.android.RestApplication.Priority;
import org.fern.rest.android.dataObj.DataEventReceiver;
import org.fern.rest.android.dataObj.DataHandler;
import org.fern.rest.android.user.User;
import org.fern.rest.fling.FlingHandler;
import org.fern.rest.fling.MyGestureDetector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to display the Task as a readable source.
 * 
 * @author Andrew
 */
public class DescriptionActivity extends Activity implements OnTouchListener, FlingHandler{
    private Task mTask = null;
    private RestApplication mApplication;
    private LinearLayout mTaskTitle;
    private LinearLayout mTaskProgress;
    private LinearLayout mTaskDateAdded;
    private LinearLayout mTaskDateModified;
    private LinearLayout mTaskDateActivated;
    private LinearLayout mTaskDateExpired;
    private LinearLayout mTaskDuration;
    private TextView mTaskParents;
    private TextView mTaskChildren;
    private LinearLayout mTaskDetails;
    private User mUser;
    private final int SAVE_ACTIVITY_CODE = 12345;
    private DataHandler mDataHandler = null;
    private GestureDetector gestureDetector;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO (DescriptionActivity) Add Description links
        super.onCreate(savedInstanceState);

        mApplication = (RestApplication) getApplication();

        mDataHandler = new DataHandler(this,new DataEventReceiver() {
           @Override
            public void onReceiveTask(Task task, CommandResult cr) {
                super.onReceiveTask(task, cr);
                
                setProgressBarIndeterminateVisibility(false);
                notifyTaskChanged();
                mTask = task;
            	setTitle(mTask.getName());
                if (cr != CommandResult.SUCCESS) {
                	switch(cr){
                	case RESOURCENOTFOUND:
                		Toast.makeText(getApplicationContext(),"User URI does not exist on server",Toast.LENGTH_SHORT).show();
                		break;
                	case AUTHENTICATION:
                		Toast.makeText(getApplicationContext(),"Invalid username or password",Toast.LENGTH_SHORT).show();
                		break;
                	case NETWORK_ERROR:
                	case HOSTNOTFOUND:
                		Toast.makeText(getApplicationContext(),"Unable to connect to the web server",Toast.LENGTH_SHORT).show();
                		break;
                	case OTHER:
                		Toast.makeText(getApplicationContext(),"Unknown error",Toast.LENGTH_SHORT).show();
                		break;
                	}
                }
           }
        });

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        Intent intent = getIntent();
        mTask = (Task) intent.getParcelableExtra("task");
        
        setTitle(mTask.getName());
        try {
            setContentView(R.layout.description_layout);
        } catch (Exception ex) {
            Log.wtf("TAG", "MESSAGE", ex);
        }
        
        mUser = (User)intent.getParcelableExtra("user");// user info needed to anthenticate with server
        mTaskTitle = (LinearLayout) findViewById(R.id.descriptionTitleLayout);
        mTaskProgress = (LinearLayout) findViewById(R.id.descriptionProgressLayout);
        mTaskDateAdded = (LinearLayout) findViewById(R.id.descriptionDateAddedLayout);
        mTaskDateModified = (LinearLayout) findViewById(R.id.descriptionDateModifiedLayout);
        mTaskDateActivated = (LinearLayout) findViewById(R.id.descriptionDateActivatedLayout);
        mTaskDateExpired = (LinearLayout) findViewById(R.id.descriptionDateExpiredLayout);
        mTaskDuration = (LinearLayout) findViewById(R.id.descriptionDurationLayout);
        mTaskParents = (TextView) findViewById(R.id.descriptionParentsLayout);
        mTaskChildren = (TextView) findViewById(R.id.descriptionChildrenLayout);
		mTaskDetails = (LinearLayout) findViewById(R.id.descriptionDetailsLayout);


        notifyTaskChanged();

        View v = this.findViewById(R.id.descriptionLineLayout);
        v.setOnTouchListener(this);
        
        gestureDetector = new GestureDetector(new MyGestureDetector(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_description_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.taskDescriptionMenuEdit:
            	Intent intent = new Intent(this, TaskEditActivity.class);
            	intent.putExtra("user", mUser);
            	intent.putExtra("task", mTask);
                this.startActivityForResult(intent, SAVE_ACTIVITY_CODE);
                break;
            case R.id.taskDescriptionMenuRefresh:
            	setProgressBarIndeterminateVisibility(true);
            	mDataHandler.refreshTask(mUser,mTask);
                break;
            case R.id.taskDescriptionMenuDelete:
            	Intent i = getIntent();
            	i.putExtra("task", mTask);
            	setResult(RESULT_OK, i);
				finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode==RESULT_OK && requestCode==SAVE_ACTIVITY_CODE){
    		Task t = (Task) data.getParcelableExtra("task");
    		if ( t != null){
    			mTask = t;
    			setTitle(mTask.getName());
    			notifyTaskChanged();
    		}
    	}
    }
    

    public void notifyTaskChanged() {
    	
    	
    	//descriptionLineLayout

        if (mTask == null) {
            mTaskTitle.setVisibility(View.GONE);
            mTaskProgress.setVisibility(View.GONE);
            mTaskDateAdded.setVisibility(View.GONE);
            mTaskDateModified.setVisibility(View.GONE);
            mTaskDateActivated.setVisibility(View.GONE);
            mTaskDateExpired.setVisibility(View.GONE);
            mTaskDuration.setVisibility(View.GONE);
            mTaskParents.setVisibility(View.GONE);
            mTaskChildren.setVisibility(View.GONE);
            mTaskDetails.setVisibility(View.GONE);
        } else {
            /* Task Title stuff */
            TextView name = (TextView) mTaskTitle
                    .findViewById(R.id.taskTitleName);
            TextView status = (TextView) mTaskTitle
                    .findViewById(R.id.taskTitleStatus);
            TextView tags = (TextView) mTaskTitle
                    .findViewById(R.id.taskTitleTags);
            ImageView priority = (ImageView) mTaskTitle
                    .findViewById(R.id.taskTitlePriority);

            name.setText(mTask.getName());
            priority.setImageResource(getDrawableResFromPriority(mTask.getPriority()));

            ((GradientDrawable) priority.getDrawable()).setColor(getColorFromPriority(mTask.getPriority()));

            if (TextUtils.isEmpty(mTask.getStatus())) {
                status.setVisibility(View.GONE);
            } else {
                status.setVisibility(View.VISIBLE);
                status.setText(mTask.getStatus());
            }

            if (mTask.getTags().size() == 0) {
                tags.setVisibility(View.GONE);
            } else {
                tags.setVisibility(View.VISIBLE);
                tags.setText("Tags: " + TextUtils.join(", ", mTask.getTags()));
            }

            /* Task Progress Stuff */
            ProgressBar progress = (ProgressBar) mTaskProgress
                    .findViewById(R.id.taskProgressBar);
            TextView progressAmt = (TextView) mTaskProgress
                    .findViewById(R.id.taskProgressAmount);
            int prog = mTask.getProgress() == null ? 0 : mTask.getProgress();
            
            ClipDrawable clip = (ClipDrawable) ((LayerDrawable) progress
                    .getProgressDrawable())
                    .findDrawableByLayerId(android.R.id.progress);
            clip.setColorFilter(getColorFromProgress(prog),
                    PorterDuff.Mode.SRC_OVER);
            progress.setProgress(prog);
            progressAmt.setText(Integer.toString(prog));

            /* Task Date Added Stuff */
            if (mTask.getAdditionDate() == null) {
                mTaskDateAdded.setVisibility(View.GONE);
            } else {
                mTaskDateAdded.setVisibility(View.VISIBLE);
                TextView dateTitle = (TextView) mTaskDateAdded
                        .findViewById(R.id.taskDateTitle);
                TextView dateMessage = (TextView) mTaskDateAdded
                        .findViewById(R.id.taskDateValue);
                dateTitle.setText(R.string.taskDateAdded);
                dateMessage.setText(mApplication.getFuzzyTimeSpan(mTask
                        .getAdditionDate()));
            }

            /* Task Date Modified Stuff */
            if (mTask.getModifiedDate() == null) {
                mTaskDateModified.setVisibility(View.GONE);
            } else {
                mTaskDateModified.setVisibility(View.VISIBLE);
                TextView dateTitle = (TextView) mTaskDateModified
                        .findViewById(R.id.taskDateTitle);
                TextView dateMessage = (TextView) mTaskDateModified
                        .findViewById(R.id.taskDateValue);
                dateTitle.setText(R.string.taskDateModified);
                dateMessage.setText(mApplication.getFuzzyTimeSpan(mTask
                        .getModifiedDate()));
            }

            /* Task Date Activated Stuff */
            if (mTask.getActivatedDate() == null) {
                mTaskDateActivated.setVisibility(View.GONE);
            } else {
                mTaskDateActivated.setVisibility(View.VISIBLE);
                TextView dateTitle = (TextView) mTaskDateActivated
                        .findViewById(R.id.taskDateTitle);
                TextView dateMessage = (TextView) mTaskDateActivated
                        .findViewById(R.id.taskDateValue);
                if (mTask.getActivatedDate().getTime() <= System
                        .currentTimeMillis()) {
                    dateTitle.setText(R.string.taskDateActivatedPast);
                } else {
                    dateTitle.setText(R.string.taskDateActivatedFuture);
                }
                dateMessage.setText(mApplication.getFuzzyTimeSpan(mTask
                        .getActivatedDate()));
            }

            /* Task Date Expiration Stuff */
            if (mTask.getExpirationDate() == null) {
                mTaskDateExpired.setVisibility(View.GONE);
            } else {
                mTaskDateExpired.setVisibility(View.VISIBLE);
                TextView dateTitle = (TextView) mTaskDateExpired
                        .findViewById(R.id.taskDateTitle);
                TextView dateMessage = (TextView) mTaskDateExpired
                        .findViewById(R.id.taskDateValue);
                if (mTask.getExpirationDate().getTime() <= System
                        .currentTimeMillis()) {
                    dateTitle.setText(R.string.taskDateExpiredPast);
                } else {
                    dateTitle.setText(R.string.taskDateExpiredFuture);
                }
                dateMessage.setText(mApplication.getFuzzyTimeSpan(mTask
                        .getExpirationDate()));
            }

            /* Task Duration Stuff */
            if (mTask.getEstimatedCompletionTime() == null) {
                mTaskDuration.setVisibility(View.GONE);
            } else {
                mTaskDuration.setVisibility(View.VISIBLE);
                TextView dateTitle = (TextView) mTaskDuration
                        .findViewById(R.id.taskDateTitle);
                TextView dateMessage = (TextView) mTaskDuration
                        .findViewById(R.id.taskDateValue);
                dateTitle.setText(R.string.taskDateEstimatedCompletionTime);
                dateMessage.setText(mApplication.getFuzzyTimeSpan(mTask
                        .getEstimatedCompletionTime()));
            }

            /* Task Parents */
            if (mTask.getParents() == null || mTask.getParents().size() == 0) {
                mTaskParents.setVisibility(View.GONE);
            } else {
                mTaskParents.setVisibility(View.VISIBLE);
                mTaskParents.setText(R.string.taskViewParents);
            }

            /* Task Children */
            if (mTask.getChildren() == null || mTask.getChildren().size() == 0) {
                mTaskChildren.setVisibility(View.GONE);
            } else {
                mTaskChildren.setVisibility(View.VISIBLE);
                mTaskChildren.setText(R.string.taskViewChildren);
            }

            //TODO
            /* Task Details */
            if (TextUtils.isEmpty(mTask.getDetails())) {
                mTaskDetails.setVisibility(View.GONE);
            } else {
                mTaskDetails.setVisibility(View.VISIBLE);
                
                TextView title = (TextView) mTaskDetails.findViewById(R.id.taskDateTitle);
                title.setText(R.string.taskEditDetails);
                
                TextView value = (TextView) mTaskDetails.findViewById(R.id.taskDateValue);
                value.setText(mTask.getDetails());
            }
        }
    }

    private int getColorFromPriority(Priority priority) {
        float pri = -1;
        switch (priority) {
            case HIGHEST: pri = 0; break;
            case VERYHIGH: pri = 1; break;
            case HIGH: pri = 3; break;
            case MEDIUM: pri = 4; break;
            case LOW: pri = 5; break;
            case VERYLOW: pri = 6; break;
            case LOWEST: pri = 7; break;
        }
        if (pri == -1) { return 0xAAAAAAAA; }
        float[] hsv = new float[3];
        hsv[0] = ((pri) / 7) * 120;
        hsv[1] = 1;
        hsv[2] = 1;
        return Color.HSVToColor(0xff, hsv);
    }

    private int getDrawableResFromPriority(Priority priority) {
        return priority.getResId();
    }

    private int getColorFromProgress(int progress) {
        float[] hsv = new float[3];
        hsv[0] = (((float) progress) / 100) * 120; // hue
        hsv[1] = 1; // saturation
        hsv[2] = 1; // value
        return Color.HSVToColor(0xff, hsv);
    }

	@Override
	public void OnFlingLeft() {
		// TODO Auto-generated method stub
		Intent i = getIntent();
		setResult(RESULT_OK, i);
		finish();
	}

	@Override
	public void OnFlingRight() {
		//Do nothing
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return true;
    }

}