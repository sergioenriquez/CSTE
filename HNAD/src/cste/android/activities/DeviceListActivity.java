package cste.android.activities;

import static cste.android.core.HnadCoreService.Events.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import cste.android.R;
import cste.android.core.HnadCoreService;
import cste.hnad.Device;

/***
 * Displays a list of all the devices currently or previously visible
 * @author User
 *
 */
public class DeviceListActivity extends Activity {
	static final String TAG = "DeviceList";
	
	private DeviceListAdapter mDeviceListAdapter;
	private ListView mDeviceListView;
	private boolean mIsBound = false;
	private HnadCoreService mHnadCoreService = null;
	private CheckBox mUsbCheckbox;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicelist);

        mDeviceListAdapter = new DeviceListAdapter(this,R.layout.devicelistitem);
        mDeviceListView = (ListView) findViewById(R.id.devicesList);
        mDeviceListView.setAdapter(mDeviceListAdapter);
        
        mUsbCheckbox = (CheckBox)findViewById(R.id.usblink);
        
        mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
            	Device dev = mDeviceListAdapter.getItem(position);
        	  	Intent intent = new Intent(getApplicationContext(), DeviceDetailsActivity.class);
        	  	intent.putExtra("deviceKey", dev.uid);
                startActivity(intent);
            }
        });

        this.registerReceiver(mDeviceUpdateReceiver, new IntentFilter(HnadCoreService.Events.HNAD_CORE_EVENT_MSG));
        doBindService();
    }
	
	private void reloadDeviceList(){
		mDeviceListAdapter.clear();
		
		Enumeration<Device> devices = mHnadCoreService.getDeviceList().elements();
		while(devices.hasMoreElements())
			mDeviceListAdapter.add(devices.nextElement());
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
		doUnbindService();  
		super.onDestroy();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:
            	startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                break;
            case R.id.logout:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            	finish();
                break;
            case R.id.eventlog:

                break;
                
            case R.id.upload:

                break;
                
            case R.id.viewtrip:

                break;
                
            case R.id.viewkeys:
                startActivity(new Intent(getApplicationContext(), DeviceKeysActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	
	
	private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if ( intent.hasExtra(DEVLIST_CHANGED) ) {
				String devChanged = intent.getStringExtra(DEVLIST_CHANGED);
				reloadDeviceList();
			}
			
			if ( intent.hasExtra(USB_STATE_CHANGED))
			{
				boolean usbState = intent.getBooleanExtra(USB_STATE_CHANGED, false);
				mUsbCheckbox.setChecked(usbState);
			}
		}
	};
	
	//TODO this should be part of a custom abstract class to avoid repetition
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mHnadCoreService = ((HnadCoreService.LocalBinder)service).getService();
			reloadDeviceList();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mHnadCoreService = null;
		}
	};
	
	void doBindService() {
		bindService(new Intent(this,HnadCoreService.class), mConnection, Context.BIND_AUTO_CREATE);    
		mIsBound = true;
	}
	
	void doUnbindService() {    
		if (mIsBound) {
			unbindService(mConnection);        
			mIsBound = false;    
		}
	}
}
