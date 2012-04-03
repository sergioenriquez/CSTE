package hnad.android.Activity;

import hnad.android.R;
import hnad.android.Service.AndroNadService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This activity sends the SIS command. The device UID must be passed via EXTRA_UID.
 * 
 * This is subclassing {@link AndroNadActivity} in order to use the custom title as well as take
 * advantage of the other setup, to get a reference to the service for sending the command.
 * It does not consume data from the {@link AndroNadService}.
 * @author Cory Sohrakoff
 *
 */
public class TripStateActivity extends AndroNadActivity {
	// For debugging
	private static final String TAG = TripStateActivity.class.getName();
	private static final boolean D = true;
	
    public static final String EXTRA_UID = "UID";
    
    // which device to command
    private String mDeviceUid;
    
    // list view of states
    private ListView mStateListView;
    
    // integer values for the states
    private int[] stateValues;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        if (getIntent() != null) {
        	mDeviceUid = getIntent().getStringExtra(EXTRA_UID);
        }
        
        setLeftTitle(R.string.sis);
        
        stateValues = getResources().getIntArray(R.array.state_values);
        String[] stateTitles = getResources().getStringArray(R.array.state_titles);
        
        // set up main layout
        setContentView(R.layout.trip_state);
        mStateListView = (ListView) findViewById(R.id.state_list_view);
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stateTitles);
        mStateListView.setAdapter(adapter);
        mStateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
		        if (getAndroNadService() != null)
		        	// get state value and send the command, then close activity
		        	getAndroNadService().sendSetIntripState(mDeviceUid, (byte) (stateValues[position] & 0xff));
		        else
		           	Toast.makeText(TripStateActivity.this, R.string.toast_command_failure, Toast.LENGTH_SHORT).show();
	        	finish();
			}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_state_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {
        case R.id.close:
        	finish();
        	return true;
        }
        return false;
    }
}
