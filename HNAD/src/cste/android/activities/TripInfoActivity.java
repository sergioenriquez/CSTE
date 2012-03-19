package cste.android.activities;

import static cste.icd.general.Utility.hexToStr;
import static cste.icd.general.Utility.strToHex;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.InputFilter;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.SettingsKey;
import cste.icd.components.ComModule;
import cste.misc.HexKeyListener;

public class TripInfoActivity extends HnadBaseActivity{
	
	private ArrayAdapter<String> mWaypointAdapter;
	private ListView mListView;
	private EditText msgInput;
	private EditText conveyanceTxt;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_info_layout);
        
        conveyanceTxt = (EditText)findViewById(R.id.conveyanceID);
        mListView = (ListView)findViewById(R.id.waypointListView);
        mWaypointAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);        
        mListView.setAdapter(mWaypointAdapter);
        registerForContextMenu(mListView);

        setWindowTitle(R.string.tripinfo_title);
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {

	}

	@Override
	protected void onCoreServiceCBound() {
		List<String> waypoints = this.mHnadCoreService.getWaypointList();
		mWaypointAdapter.clear();
		for(String s: waypoints)
			mWaypointAdapter.add(s);
		conveyanceTxt.setText(mHnadCoreService.getConveyanceIDStr());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.waypoint_menu, menu);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.waypoint_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    protected void onStop() {
    	super.onStop();
    	
    	String conveyance = conveyanceTxt.getText().toString();
    	ArrayList<String> waypoints = new ArrayList<String>();
    	
    	for(int i=0; i < mWaypointAdapter.getCount() ; i++)
    		waypoints.add(mWaypointAdapter.getItem(i));
    	mHnadCoreService.saveWaypointSettings(conveyance,waypoints);
    }
	
	private void showNewWaypointPrompt(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		msgInput = new EditText(this);
    	msgInput.setFilters(new InputFilter[]{
    	         new InputFilter.LengthFilter(32),
    	         new InputFilter.AllCaps()
    	         });
    	msgInput.requestFocus();
    	msgInput.setHint("GPS Location");
    	alert.setView(msgInput);
    	alert.setTitle("Enter new waypoint");
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    		  String value = msgInput.getText().toString();
    		  if( value != "")
    			  mWaypointAdapter.add(value);
    		  
    		  InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		  imm.hideSoftInputFromWindow(msgInput.getWindowToken(), 0);

    		}
    	});
    	alert.show();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
        switch (item.getItemId()) {
            case R.id.remove:
            	int c = mWaypointAdapter.getCount();
            	if(c>0)
            		mWaypointAdapter.remove(mWaypointAdapter.getItem(c-1));
                break;
            case R.id.addNew:
            	showNewWaypointPrompt();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    
	    String waypoint = mWaypointAdapter.getItem(info.position);
	    
	    switch (item.getItemId()) {
	        case R.id.remove:
	        	mWaypointAdapter.remove(waypoint);
	            return true;
	        case R.id.addNew:
	        	showNewWaypointPrompt();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
