package cste.android.activities;

import java.util.Enumeration;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.components.ComModule;
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;

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
        mDeviceListAdapter.setNotifyOnChange(true);
        mDeviceListView = (ListView) findViewById(R.id.devicesList);
        
        mDeviceListView.setAdapter(mDeviceListAdapter);
        registerForContextMenu(mDeviceListView);

        mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
            	ComModule dev = mDeviceListAdapter.getItem(position);
        	  	Intent intent = new Intent(getApplicationContext(), ECoCInfoActivity.class);
        	  	intent.putExtra("device",(Parcelable) dev);
                startActivity(intent);
            }
        });
        
        
      IntentFilter filter = new IntentFilter();
      filter.addAction(Events.DEVICE_INFO_CHANGED);
      filter.addAction(Events.DEVLIST_CHANGED);
      registerReceiver(mDeviceUpdateReceiver, filter); 
    }
	
	@Override
	protected void onCoreServiceCBound(){
		reloadDeviceList();
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		if ( action.equals(Events.DEVLIST_CHANGED) ) {
			reloadDeviceList();
		}

		if (  action.equals(Events.DEVICE_INFO_CHANGED)){
			DeviceUID devUID = (DeviceUID)intent.getSerializableExtra("deviceUID");
			ComModule changedCm = (ComModule)mHnadCoreService.getDeviceRecord(devUID);
			if( changedCm == null)
				return;
			
			int cnt = mDeviceListView.getCount();
			
			for(int i=0;i<cnt; i++){
				ComModule old = (ComModule)mDeviceListView.getItemAtPosition(i);
				if( old.UID().equals(changedCm.UID())){
					mDeviceListAdapter.remove(old);
					mDeviceListAdapter.add(changedCm);
					mDeviceListAdapter.notifyDataSetChanged();
					break;
				}
			}       	
		}
		
		//if ( data.containsKey(USB_STATE_CHANGED))
		//	{
			//boolean usbState = data.getBoolean(USB_STATE_CHANGED, false);
			//mUsbCheckbox.setChecked(usbState);
		//}
	}
	
	private void reloadDeviceList(){
		mDeviceListAdapter.clear();
		
		Enumeration<ComModule> devices = mHnadCoreService.getDeviceList().elements();
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.device_info_menu, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
            	startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
                break;
            case R.id.logout:
            	mHnadCoreService.logout();
            	Intent logoutIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logoutIntent);
            	finish();
                break;
            case R.id.eventlog: // 
            	Intent eventLogIntent = new Intent(getApplicationContext(), EventLogHNADActivity.class);
                startActivity(eventLogIntent);
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
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    
	    ComModule cm = mDeviceListAdapter.getItem(info.position);
	    
	    switch (item.getItemId()) {
		    case R.id.viewEventLog:
	        	Intent eventLogIntent = new Intent(getApplicationContext(), EventLogECMActivity.class);
	        	eventLogIntent.putExtra("device", (Parcelable) cm);
	    		startActivity(eventLogIntent);
	    		return true;
	        case R.id.refresh:
	        	mHnadCoreService.sendDevCmd(cm.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
	            return true;
	        case R.id.clearAlarm:

	            return true;
	        case R.id.erase:
	        	mHnadCoreService.deleteDeviceRecord(cm.UID());
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
