package cste.android.activities;

import java.util.ArrayList;

import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.SettingsKey;
import cste.hnad.EcocDevice;
import cste.messages.EventLogICD;
import cste.misc.EventLogRowICD;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class EventLogECMActivity extends HnadBaseActivity {
	private static final String TAG = "ECM Log";
	TableLayout  mTable;
	EcocDevice mECoCDev;
	private ProgressDialog pd; // TODO move this to base class
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventlog);
        pd = new ProgressDialog(this);
        mECoCDev = getIntent().getParcelableExtra("device"); 
        mTable = (TableLayout)findViewById(R.id.devTable);
        
	}
	
	protected void reloadLogScreen(){
		pd = ProgressDialog.show(this, "Retrieving records..", "" , true, true);
		mTable.removeAllViews();
		mTable.addView(new EventLogRowICD(this)); // title row
		ArrayList<EventLogICD> devLog= mHnadCoreService.getDeviceEventLog(mECoCDev.UID());
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
	protected void handleCoreServiceMsg(Context context, Bundle data) {

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
            	mHnadCoreService.sendDevCmd(mECoCDev.UID(), DeviceCommands.GET_EVENT_LOG);
                break;
            case R.id.clear:
            	mHnadCoreService.deleteDeviceLogs(mECoCDev.UID());
            	finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
