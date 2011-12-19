package hnad.android.Activity;

import hnad.android.R;
import hnad.android.ICD.DeviceInfo;
import hnad.android.ListAdapter.TwoLineWithImageArrayAdapter;
import hnad.android.Service.AndroNadService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * This activity handles the UI for the app. It also starts the {@link AndroNadService}, if not
 * already running.
 * 
 * @author Cory Sohrakoff
 *
 */
public class MainActivity extends AndroNadActivity {
	// For debugging
    private static final String TAG = MainActivity.class.getName();
    private static final boolean D = true;

    // Layout Views   
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        setLeftTitle(R.string.app_name);
        
        // Set up main views
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				DeviceInfo device = (DeviceInfo) parent.getItemAtPosition(position);
				Intent activity = new Intent(MainActivity.this, DeviceInfoActivity.class);
				// send along UID so the activity can get the device info
    			activity.putExtra(DeviceInfoActivity.EXTRA_UID, device.getUID());
    			startActivity(activity);
			}
		});
        
        TwoLineWithImageArrayAdapter<DeviceInfo> adapter = new TwoLineWithImageArrayAdapter<DeviceInfo>(this, null);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {    	
		final MenuItem disconnectItem = menu.findItem(R.id.disconnect);
		disconnectItem.setVisible(getAndroNadService() != null);
		
		final MenuItem settingsItem = menu.findItem(R.id.settings);
		settingsItem.setVisible(getAndroNadService() != null);
		
		final MenuItem connectCommandItem = menu.findItem(R.id.connect);
		connectCommandItem.setVisible(getAndroNadService() == null);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.connect:
            // Launch the AndroNadService
        	startAndroNadService();
        	return true;
        case R.id.disconnect:
        	// Stop service to disconnect
        	stopAndroNadService();
        	return true;
        case R.id.settings:
			Intent activity = new Intent(this, SettingsActivity.class);
			startActivity(activity);
			return true;
        }
        return false;
    }
	
	/**
	 * Update the list view with the latest data from the BluetoothService.
	 * @param service
	 */
	private void updateListView(AndroNadService service) {
		if (service != null) {
			listView.setEnabled(false);
			TwoLineWithImageArrayAdapter<DeviceInfo> adapter = new TwoLineWithImageArrayAdapter<DeviceInfo>(MainActivity.this, service.getAllDeviceInfo());
	        listView.setAdapter(adapter);
	        listView.setEnabled(true);
		}	
	}

	@Override
	protected void onDataUpdated(AndroNadService service) {
		updateListView(service);
	}

	@Override
	protected void onServiceConnected(AndroNadService service) {
		updateListView(service);
	}


}