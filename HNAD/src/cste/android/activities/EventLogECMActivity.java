package cste.android.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.icd.icd_messages.EventLogICD;
import cste.icd.types.DeviceUID;

public class EventLogECMActivity extends HnadBaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "ECM Log";

	protected EcocLogAdapter mLogAdapter;
	protected ListView mLogListView;
	protected DeviceUID devUID;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecoc_log_layout);
        setWindowTitle(R.string.eventlog_title);
        
        mLogAdapter = new EcocLogAdapter(this,R.layout.ecoc_log_item);
        mLogAdapter.setNotifyOnChange(true);
        mLogListView = (ListView) findViewById(R.id.logList);
        mLogListView.setAdapter(mLogAdapter);

        devUID = (DeviceUID)getIntent().getSerializableExtra("deviceUID"); 

        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.TRANSMISSION_RESULT);
		filter.addAction(Events.ECM_EVENTLOG_CHANGE);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}
	
	protected void reloadLogScreen(){
		mLogAdapter.clear();
		
		ArrayList<EventLogICD> devLog= mHnadCoreService.getEcmEventLog(devUID);
		for(EventLogICD log: devLog){
			mLogAdapter.add(log);
		}
	}
	
	@Override
    protected void onCoreServiceCBound(){
		reloadLogScreen();
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		DeviceUID changedDevUID = (DeviceUID)intent.getSerializableExtra("deviceUID");
		if( devUID == null || !changedDevUID.equals(devUID)){
			return;
		} 
		
		if ( action.equals(Events.ECM_EVENTLOG_CHANGE) ) {
			reloadLogScreen();
			pd.cancel();
		}
		
		if ( action.equals(Events.TRANSMISSION_RESULT) ){
			boolean result = intent.getBooleanExtra("result", false);
			if( result == false)
				pd.cancel();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.log_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
            	mHnadCoreService.sendDevCmd(devUID, DeviceCommands.GET_EVENT_LOG);
            	showProgressDialog("Requesting records...");
                break;
            case R.id.clear:
            	mHnadCoreService.deleteDeviceLogs(devUID);
            	reloadLogScreen();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
