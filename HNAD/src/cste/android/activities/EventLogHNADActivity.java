package cste.android.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;
import cste.android.R;
import cste.android.core.HNADService.Events;
import cste.misc.HnadEventLog;

public class EventLogHNADActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Event Log Activity";
	
	protected HnadLogAdapter mHnadLogAdapter;
	protected ListView mLogListView;
	//TableLayout  mTable;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hnad_log_layout);
        setWindowTitle(R.string.eventlog_title);
        //mTable = (TableLayout)findViewById(R.id.devTable);
        
        mHnadLogAdapter = new HnadLogAdapter(this,R.layout.hnad_log_item);
        mHnadLogAdapter.setNotifyOnChange(true);
        mLogListView = (ListView) findViewById(R.id.logList);
        mLogListView.setAdapter(mHnadLogAdapter);
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.HNAD_EVENTLOG_CHANGE);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}

	protected void reloadLogScreen(){
//		mTable.removeAllViews();
//		mTable.addView(new EventLogRowHnad(this)); // title row
		
		mHnadLogAdapter.clear();
		ArrayList<HnadEventLog> devLog = mHnadCoreService.getHnadEventLog();
		for(HnadEventLog l: devLog){
			mHnadLogAdapter.add(l);
		}
		
//		for(HnadEventLog log: devLog){
//			mTable.addView(new EventLogRowHnad(this,log));
//		}

	}

	@Override
	protected void onCoreServiceCBound() {
		reloadLogScreen();
	}
	
	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		if ( action.equals(Events.HNAD_EVENTLOG_CHANGE) ) {
			reloadLogScreen();
		}
	}
}
