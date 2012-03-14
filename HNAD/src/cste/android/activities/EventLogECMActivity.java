package cste.android.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;
import cste.messages.EventLogICD;
import cste.misc.EventLogRowICD;

public class EventLogECMActivity extends HnadBaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "ECM Log";
	TableLayout  mTable;
	DeviceUID devUID;
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_log_layout);

        devUID = (DeviceUID)getIntent().getSerializableExtra("deviceUID"); 
        mTable = (TableLayout)findViewById(R.id.devTable);
        
        setWindowTitle(R.string.eventlog_title);
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.TRANSMISSION_RESULT);
		filter.addAction(Events.DEV_EVENT_LOG_CHANGD);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}
	
	protected void reloadLogScreen(){
		mTable.removeAllViews();
		mTable.addView(new EventLogRowICD(this)); // title row
		ArrayList<EventLogICD> devLog= mHnadCoreService.getDeviceEventLog(devUID);
		for(EventLogICD log: devLog){
			mTable.addView(new EventLogRowICD(this,log));
		}
		pd.cancel();
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
		
		if ( action.equals(Events.DEV_EVENT_LOG_CHANGD) ) {
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
