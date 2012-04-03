package org.fern.rest.android;

import org.fern.rest.android.dataObj.DataHandler;
import org.fern.rest.android.task.Task;
import org.fern.rest.android.task.TasksActivity;
import org.fern.rest.android.task.description.DescriptionActivity;
import org.fern.rest.android.user.User;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AccountsActivity extends Activity implements OnTouchListener, FlingHandler {
    private List<User> mAccounts;
    private ListView mAccountsList;
    private DataHandler mHandler;
    private AccountListAdapter mAdapter;
    private GestureDetector gestureDetector;
    private int mLastItemPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new DataHandler(this, null);
        mAccounts = mHandler.getUserList();
        
        setContentView(R.layout.accounts_layout);
        mAccountsList = (ListView) findViewById(R.id.accountsList);
        mAccountsList.setTextFilterEnabled(true);
        mAdapter = new AccountListAdapter(this, R.layout.account_item, mAccounts);
        mAccountsList.setAdapter(mAdapter);
        
        
        mAccountsList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
            	mLastItemPosition = position;
                User user = mAdapter.getItem(position);
                Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        
        if ( mAccountsList.getCount() > 0)
        	mLastItemPosition = 0;
        
        findViewById(R.id.accountsList).setOnTouchListener(this);
        gestureDetector = new GestureDetector(new MyGestureDetector(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.accounts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.accountsMenuLogin:
                intent = new Intent(this, AuthenticateActivity.class);
                startActivity(intent);
                break;
            case R.id.accountsMenuCreateNew:
//                intent = new Intent(this, CreateAccountActivity.class);
//                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void OnFlingLeft() {
		//Nothing
	}

	@Override
	public void OnFlingRight() {
		if ( mLastItemPosition != -1){
			User user = mAdapter.getItem(mLastItemPosition);
	        Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
	        intent.putExtra("user", user);
	        startActivity(intent);
		}
	}
}
