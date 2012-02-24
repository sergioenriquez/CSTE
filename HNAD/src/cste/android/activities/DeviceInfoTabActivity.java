package cste.android.activities;

import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import cste.android.R;
import cste.android.core.HnadCoreService;

/***
 * This activity has three tabs for organizing the device information and commands
 * @author Sergio Enriquez
 *
 */
public class DeviceInfoTabActivity extends TabActivity  {
	static final String TAG = "Device Info Tabs";
	
	@Override
    public void onResume() {
		super.onResume();
		Log.i(TAG,"resumed");	
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Log.i(TAG,"paused");
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicedetails);
        
        Intent intent =  getIntent();
        //If user got here by clicking the new device notification, remove that item
        if( intent.hasExtra("clearNotifications"));
        {
        	NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        	manager.cancel(HnadCoreService.NEW_DEVICE_NOTIFICATION);
        }

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        intent.setClass(this, DeviceDetailsActivity.class);
        spec = tabHost.newTabSpec("Info").setIndicator(
        		"Info",
        		res.getDrawable(R.drawable.ic_tab_devinfo))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent.setClass(this, DeviceCmdActivity.class);
        spec = tabHost.newTabSpec("Commands").setIndicator(
        		"Commands",
        		res.getDrawable(R.drawable.ic_tab_devcmd))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent.setClass(this, DeviceLogActivity.class);
        spec = tabHost.newTabSpec("Event Log").setIndicator(
        		"Log",
        		res.getDrawable(R.drawable.ic_menu_devlog)).
        		setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.devicedetails_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eventlog:

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
