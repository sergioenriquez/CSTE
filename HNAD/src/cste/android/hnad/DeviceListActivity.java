package cste.android.hnad;

import java.util.ArrayList;
import java.util.List;
import cste.android.R;
import cste.hnad.Device;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import static cste.android.hnad.HnadCoreService.*;

public class DeviceListActivity extends Activity {
	
	
	private List<Device> mDeviceList; // move into handler class
	private DeviceListAdapter mDeviceListAdapter;
	private ListView mDeviceListView;
	
	private static final String DEVICE_LIST_UPDATE = "test";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicelist);
        
        mDeviceList = new ArrayList<Device>();
        mDeviceList.add(new Device(true,"aaaa","yyy"));
        mDeviceList.add(new Device(false,"bbbb","xxx"));
        
        mDeviceListAdapter = new DeviceListAdapter(this,R.layout.devicelistitem,mDeviceList );
        
        mDeviceListView = (ListView) findViewById(R.id.devicesList);
        mDeviceListView.setAdapter(mDeviceListAdapter);
        
        mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
            	Intent intent = new Intent(getApplicationContext(), DeviceDetailsActivity.class);
                startActivity(intent);
            	
//            	mLastItemPosition = position;
//                User user = mAdapter.getItem(position);
//                Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
//                intent.putExtra("user", user);
//                startActivity(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
		filter.addAction(HnadCoreService.DEVICES_UPDATED);
        this.registerReceiver(mDeviceUpdateReceiver, filter);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.devicelist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mDeviceUpdateReceiver);
		//this.stopService(new Intent(this,HnadCoreService.class));
		super.onDestroy();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
                
                break;
            case R.id.logout:

                break;
            case R.id.eventlog:

                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	
	
	private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DEVICES_UPDATED.equals(action)) {
				//TODO
				
				Toast.makeText(getApplicationContext(), String.valueOf(intent.getIntExtra("itemChanged", 0)), Toast.LENGTH_SHORT).show();
				
			}
		}
	};
}
