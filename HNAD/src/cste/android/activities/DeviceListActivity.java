package cste.android.activities;

import java.util.Enumeration;

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
import android.widget.ListView;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.icd.components.ComModule;
import cste.icd.types.DeviceUID;

/***
 * Displays a list of all the devices currently or previously visible
 * @author User
 *
 */
public class DeviceListActivity extends HnadBaseActivity {
	static final String TAG = "DeviceList";
	
	protected DeviceListAdapter mDeviceListAdapter;
	protected ListView mDeviceListView;
	boolean discoveryMode = false;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicelist_layout);

        mDeviceListAdapter = new DeviceListAdapter(this,R.layout.devicelistitem);
        mDeviceListAdapter.setNotifyOnChange(true);
        mDeviceListView = (ListView) findViewById(R.id.devicesList);
        mDeviceListView.setAdapter(mDeviceListAdapter);
        registerForContextMenu(mDeviceListView);

        mDeviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            	ComModule dev = mDeviceListAdapter.getItem(position);
        	  	Intent intent = new Intent(getApplicationContext(), ECoCInfoActivity.class);
        	  	intent.putExtra("deviceUID",dev.devUID);
                startActivity(intent);
            }
        });
        
        setWindowTitle(R.string.devlist_title);
        
		IntentFilter filter = new IntentFilter();
		filter.addAction(Events.DEVICE_INFO_CHANGED);
		filter.addAction(Events.DEVLIST_CHANGED);
		filter.addAction(Events.TRANSMISSION_RESULT);
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
		
		if ( action.equals(Events.TRANSMISSION_RESULT) ) {
			pd.cancel();
		}

		if (  action.equals(Events.DEVICE_INFO_CHANGED)){
			DeviceUID devUID = (DeviceUID)intent.getSerializableExtra("deviceUID");
//			this.reloadDeviceList();
//			mDeviceListAdapter.notifyDataSetChanged();
			ComModule changedCm = (ComModule)mHnadCoreService.getDeviceRecord(devUID);
			if( changedCm == null)
				return;
			
			int cnt = mDeviceListView.getCount();
			for(int i=0;i<cnt; i++){
				ComModule old = (ComModule)mDeviceListView.getItemAtPosition(i);
				if( old.devUID.equals(changedCm.devUID)){
					//old = changedCm;
					mDeviceListAdapter.remove(old);
					mDeviceListAdapter.add(changedCm);
					mDeviceListAdapter.sortList();
					mDeviceListAdapter.notifyDataSetChanged();
					break;
				}
			}      
			
		}
	}
	
	private void reloadDeviceList(){
		mDeviceListAdapter.clear();
		
		Enumeration<ComModule> devices = mHnadCoreService.getDeviceList().elements();
		while(devices.hasMoreElements())
			mDeviceListAdapter.add(devices.nextElement());
		
		mDeviceListAdapter.sortList();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.devicelist_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		//menu.clear();
		menu.removeItem(R.id.discovery);
		if(discoveryMode)
        	menu.add(0,R.id.discovery,0,R.string.discoveryON);
        else
        	menu.add(0,R.id.discovery,0,R.string.discoveryOFF);

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.device_context_menu, menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
        switch (item.getItemId()) {
            case R.id.settings:
            	intent = new Intent(getApplicationContext(), ConfigActivity.class);
            	startActivity(intent);
                break;
            case R.id.logout:
            	mHnadCoreService.logout();
            	intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            	finish();
                break;
            case R.id.eventlog:
            	intent = new Intent(getApplicationContext(), EventLogHNADActivity.class);
            	startActivity(intent);
                break;    
            case R.id.getKeys:
//            	Intent viewKeysIntent = new Intent(getApplicationContext(), KeyViewActivity.class);
//	    		startActivity(viewKeysIntent);
                break;
            case R.id.viewtrip:
            	intent = new Intent(getApplicationContext(), TripInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.discovery:
            	discoveryMode = !discoveryMode;
            	mHnadCoreService.toggleDiscoveryMode(discoveryMode);
            	break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    ComModule cm = mDeviceListAdapter.getItem(info.position);
	    
	    switch (item.getItemId()) {
	        case R.id.refresh:
	        	mHnadCoreService.sendDevCmd(cm.devUID,DeviceCommands.GET_RESTRICTED_STATUS);
	        	showProgressDialog("Querying device status...");
	            return true;
	        case R.id.viewEventLog:
	        	Intent eventLogIntent = new Intent(getApplicationContext(), EventLogECMActivity.class);
	        	eventLogIntent.putExtra("deviceUID", cm.devUID);
	    		startActivity(eventLogIntent);
	    		return true;
	        case R.id.clearAlarm:
	        	mHnadCoreService.sendDevCmd(cm.devUID,DeviceCommands.SET_ALARM_OFF);
	        	showProgressDialog("Clearing alarms...");
	            return true;
	        case R.id.commission:
	        	mHnadCoreService.sendDevCmd(cm.devUID,DeviceCommands.SET_COMMISION_ON);
	        	showProgressDialog("Commisioning...");
	        	return true;
	        case R.id.editKeys:
	        	Intent editkeysIntent = new Intent(getApplicationContext(), EditKeysActivity.class);
	        	editkeysIntent.putExtra("deviceUID", cm.devUID);
	    		startActivity(editkeysIntent);
	            return true;
	        case R.id.setTime:
	        	showProgressDialog("Setting time on device...");
	            return true;
	        case R.id.setWaypoints:
	        	mHnadCoreService.sendDevCmd(cm.devUID,DeviceCommands.SET_WAYPOINTS_START);
	        	showProgressDialog("Setting waypoints...");
	            return true;
	        case R.id.getUnrestricted:
	        	mHnadCoreService.sendDevCmd(cm.devUID,DeviceCommands.GET_UN_RESTRICTED_STATUS);
	        	showProgressDialog("Requesting status...");
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
}
