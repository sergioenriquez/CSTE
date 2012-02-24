package cste.android.activities;

import static cste.android.core.HnadCoreService.Events.DEVLIST_CHANGED;
import static cste.android.core.HnadCoreService.Events.USB_STATE_CHANGED;

import java.util.Enumeration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import cste.android.R;
import cste.hnad.Device;

/***
 * Displays a list of all the devices currently or previously visible
 * @author User
 *
 */
public class DeviceListActivity extends HnadBaseActivity {
	static final String TAG = "DeviceList";
	
	private DeviceListAdapter mDeviceListAdapter;
	private ListView mDeviceListView;
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
        	  	Intent intent = new Intent(getApplicationContext(), DeviceInfoTabActivity.class);
        	  	intent.putExtra("device", dev);
                startActivity(intent);
            }
        });
    }
	
	@Override
	protected void onCoreServiceCBound()
	{
		reloadDeviceList();
	}
	

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {
		if ( data.containsKey(DEVLIST_CHANGED) ) {
			//String devChanged = data.getString(DEVLIST_CHANGED);
			reloadDeviceList();
		}
		
		if ( data.containsKey(USB_STATE_CHANGED))
		{
			boolean usbState = data.getBoolean(USB_STATE_CHANGED, false);
			mUsbCheckbox.setChecked(usbState);
		}
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
}
