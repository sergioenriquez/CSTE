package hnad.android.Activity;

import hnad.android.R;
import hnad.android.Service.AndroNadService;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceLogActivity extends AndroNadActivity {
	// For debugging
    private static final String TAG = DeviceLogActivity.class.getName();
    private static final boolean D = true;
    
    private ListView mLogListView;
    
    // The device being viewed   
    private String mDeviceUid;
    
    // extra to pass UID to activity via intent when starting
    public static final String EXTRA_UID = "UID";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        setLeftTitle(R.string.device_log);
        
        if (getIntent() != null) {
        	mDeviceUid = getIntent().getStringExtra(EXTRA_UID);
        }
        
        // set up main layout
        setContentView(R.layout.device_log);
        mLogListView = (ListView) findViewById(R.id.log_list_view);
        ListAdapter logAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.row_text_view);
        mLogListView.setAdapter(logAdapter);
        mLogListView.setClickable(false); // make it non-interactive
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_log_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {
        case R.id.close:
        	finish();
        	return true;
        case R.id.clear:
        	// clear log
        	if (getAndroNadService() != null)
        		getAndroNadService().clearDeviceEventLog(mDeviceUid);
        	else
            	Toast.makeText(this, R.string.toast_disconnected_error, Toast.LENGTH_SHORT).show();
        	return true;
        }
        return false;
    }
	
	/**
	 * Update UI with latest info from the AndroNadService.
	 * 
	 * @param service
	 */
	private void updateInfo(AndroNadService service) {
		if (service != null) {
			ArrayList<String> temp = service.getDeviceEventLog(mDeviceUid);
			if (temp != null) {
				ListAdapter logAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.row_text_view, temp);
				mLogListView.setAdapter(logAdapter);
			}
		}
	}

	@Override
	protected void onDataUpdated(AndroNadService service) {
		updateInfo(service);
	}

	@Override
	protected void onServiceConnected(AndroNadService service) {
		updateInfo(service);
	}	
}
