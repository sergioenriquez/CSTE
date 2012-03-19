package cste.android.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TableLayout;
import cste.android.R;
import cste.android.core.HNADService.Events;
import cste.icd.icd_messages.EventLogICD;
import cste.icd.types.DeviceUID;
import cste.misc.EventLogRowHnad;
import cste.misc.EventLogRowICD;
import cste.misc.HnadEventLog;

public class EventLogHNADActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Event Log Activity";
	
	TableLayout  mTable;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_log_layout);
        mTable = (TableLayout)findViewById(R.id.devTable);
        setWindowTitle(R.string.eventlog_title);
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.HNAD_EVENTLOG_CHANGE);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}


	protected void reloadLogScreen(){
		mTable.removeAllViews();
		mTable.addView(new EventLogRowHnad(this)); // title row
		ArrayList<HnadEventLog> devLog = mHnadCoreService.getHnadEventLog();
		for(HnadEventLog log: devLog){
			mTable.addView(new EventLogRowHnad(this,log));
		}
		pd.cancel();
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
