package hnad.android.Activity;

import hnad.android.R;
import hnad.android.ICD.DeviceInfo;
import hnad.android.ICD.ICD;
import hnad.android.Service.AndroNadService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity to either send ARMT or CTI commands. The ARMT or CTI opcode must be passed via the intent as
 * EXTRA_TRIP_INFO_OPCODE. The device UID must also be passed via EXTRA_UID.
 * 
 * This is subclassing {@link AndroNadActivity} in order to use the custom title as well as take
 * advantage of the other setup, to get a reference to the service for sending the command.
 * It does not consume data from the {@link AndroNadService}.
 * 
 * @author Cory Sohrakoff
 *
 */
public class TripInfoActivity extends AndroNadActivity {
	// For debugging
	private static final String TAG = TripInfoActivity.class.getName();
	private static final boolean D = true;
	
	// Extra for opcode, either ARMT or CTI
	public static final String EXTRA_TRIP_INFO_OPCODE 	= "OPCODE";
    public static final String EXTRA_UID 				= "UID";
    
    // what the command is
    private byte mOpcode;
    
    private EditText mMechSeal;
    private EditText mConveyance;
    private EditText mManifest;
    
    // which device to command
    private String mDeviceUid;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        if (getIntent() != null) {
        	mOpcode = getIntent().getByteExtra(EXTRA_TRIP_INFO_OPCODE, (byte) -1);
        	mDeviceUid = getIntent().getStringExtra(EXTRA_UID);
        	switch (mOpcode) {
			case ICD.RCMD_OPCODE_ARMT:
				setLeftTitle(R.string.armt);
				break;
			case ICD.RCMD_OPCODE_CTI:
				setLeftTitle(R.string.cti);
				break;
			default:
				setLeftTitle(R.string.trip_info);
			}
        } else {
        	setLeftTitle(R.string.trip_info);
        }
        
        // set up main layout
        setContentView(R.layout.trip_info);
        mMechSeal = (EditText) findViewById(R.id.et_mech_seal);
        mConveyance = (EditText) findViewById(R.id.et_conveyance);
        mManifest = (EditText) findViewById(R.id.et_manifest);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_info_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {
        case R.id.close:
        	finish();
        	return true;
        case R.id.send_command:
        	if (getAndroNadService() != null) {
            	switch (mOpcode) {
    			case ICD.RCMD_OPCODE_ARMT:
    				// fall through since it is the same for now
    			case ICD.RCMD_OPCODE_CTI:
    				getAndroNadService().sendTripInformation(mDeviceUid, mOpcode, 
    						mMechSeal.getText().toString(), 
    						mConveyance.getText().toString(), 
    						mManifest.getText().toString());
    				break;
            	}
        	} else {
            	Toast.makeText(this, R.string.toast_command_failure, Toast.LENGTH_SHORT).show();
        	}
        	finish();
        	return true;
        }
        return false;
    }

	@Override
	protected void onServiceConnected(AndroNadService service) {
		// if CTI, get old trip info and fill in the edit texts
		if (service != null && mOpcode == ICD.RCMD_OPCODE_CTI) {
			DeviceInfo temp = service.getDeviceInfo(mDeviceUid);
			if (temp != null) {
				mMechSeal.setText(temp.getMechanicalSealId().trim());
				mConveyance.setText(temp.getConveyanceId().trim());
				mManifest.setText(temp.getManifest().trim());
			}	
		}
		
		if (service != null) {
			mMechSeal.setEnabled(true);
			mConveyance.setEnabled(true);
			mManifest.setEnabled(true);
		}
	}

	@Override
	protected void onServiceDisconnected() {
		mMechSeal.setEnabled(false);
		mConveyance.setEnabled(false);
		mManifest.setEnabled(false);
	}	
	
	
}
