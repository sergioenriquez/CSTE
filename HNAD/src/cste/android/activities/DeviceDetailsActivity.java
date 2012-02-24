package cste.android.activities;

import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import cste.android.R;
import cste.android.core.HnadCoreService;
import cste.hnad.Device;

public class DeviceDetailsActivity extends TabActivity  {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicedetails);
        
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        
        if( getIntent().hasExtra("clearNotifications"));
        {
        	NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        	manager.cancel(HnadCoreService.NEW_DEVICE_NOTIFICATION);
        }
        
        String key = getIntent().getStringExtra("deviceKey");
        
        intent = new Intent().setClass(this, DeviceInfoActivity.class);
        intent.putExtra("deviceKey", key);
        spec = tabHost.newTabSpec("Info").setIndicator("Info",res.getDrawable(R.drawable.ic_tab_devinfo)).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, DeviceCmdActivity.class);
        spec = tabHost.newTabSpec("Commands").setIndicator("Commands",res.getDrawable(R.drawable.ic_tab_devcmd)).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, DeviceLogActivity.class);
        spec = tabHost.newTabSpec("Event Log").setIndicator("Details",res.getDrawable(R.drawable.ic_menu_devlog)).setContent(intent);
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
