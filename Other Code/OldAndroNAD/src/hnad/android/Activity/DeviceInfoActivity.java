package hnad.android.Activity;

import hnad.android.R;
import hnad.android.ICD.DeviceInfo;
import hnad.android.ICD.ICD;
import hnad.android.Service.AndroNadService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This displays the detailed device info when a device is chosen from the AndroNAD Activity's 
 * ListView. It also updates the devices info if the status changes.
 * 
 * When launching this via Intent, you need to include the device's UID in the Intent as EXTRA_UID.
 * 
 * @author Cory Sohrakoff
 *
 */
public class DeviceInfoActivity extends AndroNadActivity {
	// For debugging
    private static final String TAG = DeviceInfoActivity.class.getName();
    private static final boolean D = true;
    
    // For command dialog
    private static final int DIALOG_COMMAND 	= 1;
    
    // Command indexes for the command array defined in strings.xml
    // Need to update this if the command array changes
    private static final int COMMAND_USTATUS	=  0;
    private static final int COMMAND_NOP		=  1;
    private static final int COMMAND_SMAT		=  2;
    private static final int COMMAND_SMAF		=  3;
    private static final int COMMAND_DADC		=  4;
    private static final int COMMAND_ST			=  5;
    private static final int COMMAND_ARMT		=  6;
    private static final int COMMAND_CTI		=  7;
    private static final int COMMAND_SIS		=  8;
    private static final int COMMAND_SL			=  9;
    private static final int COMMAND_SLU		= 10;
    private static final int COMMAND_EL			= 11;
    
    private TextView mInfoTextView;
    
    // The device being viewed
    private DeviceInfo mDeviceInfo;
    
    private String mDeviceUid;
    
    // extra to pass UID to activity via intent when starting
    public static final String EXTRA_UID = "UID";
    
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        setLeftTitle(R.string.device_info);
        
        // set up main layout
        setContentView(R.layout.device_info);
        mInfoTextView = (TextView) findViewById(R.id.text_view);
        if (getIntent() != null) {
        	mDeviceUid = getIntent().getStringExtra(EXTRA_UID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_info_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {
        case R.id.close:
        	finish();
        	return true;
        case R.id.send_command:
        	if (getAndroNadService() != null)
        		showDialog(DIALOG_COMMAND);
        	else
            	Toast.makeText(this, R.string.toast_command_failure, Toast.LENGTH_SHORT).show();
        	return true;
        case R.id.log:
			Intent activity = new Intent(this, DeviceLogActivity.class);
			activity.putExtra(TripInfoActivity.EXTRA_UID, mDeviceUid);
			startActivity(activity);
			return true;
        }
        return false;
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_COMMAND:
			return createCommandDialog();
		}
		return super.onCreateDialog(id);
	}
	
	/**
	 * Create the dialog with a list of commands.
	 * @return
	 */
	private Dialog createCommandDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setTitle(R.string.command)
			.setItems(R.array.commands, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					Intent activity; // intent used to start sub activities for CTI, ARMT, and SIS
					switch (item) {
					case COMMAND_USTATUS:
						if (D) Log.i(TAG, "USTATUS command chosen.");
						getAndroNadService().sendUnrestrictedStatusCommand(mDeviceUid);
						break;
					case COMMAND_NOP:
						if (D) Log.i(TAG, "NOP command chosen.");
						getAndroNadService().sendNopCommand(mDeviceUid);
						break;
					case COMMAND_SMAT:
						if (D) Log.i(TAG, "SMAT command chosen.");
						getAndroNadService().sendSetMasterAlarm(mDeviceUid, true);
						break;
					case COMMAND_SMAF:
						if (D) Log.i(TAG, "SMAF command chosen.");
						getAndroNadService().sendSetMasterAlarm(mDeviceUid, false);
						break;
					case COMMAND_DADC:
						if (D) Log.i(TAG, "DADC command chosen.");
						getAndroNadService().sendDisarm(mDeviceUid);
						break;
					case COMMAND_ST:
						if (D) Log.i(TAG, "ST command chosen.");
						getAndroNadService().sendSetTime(mDeviceUid);
						break;
					case COMMAND_ARMT:
						if (D) Log.i(TAG, "ARMT command chosen.");
						activity = new Intent(DeviceInfoActivity.this, TripInfoActivity.class);
						activity.putExtra(TripInfoActivity.EXTRA_TRIP_INFO_OPCODE, ICD.RCMD_OPCODE_ARMT);
						activity.putExtra(TripInfoActivity.EXTRA_UID, mDeviceUid);
						startActivity(activity);
						break;
					case COMMAND_CTI:
						if (D) Log.i(TAG, "CTI command chosen.");
						activity = new Intent(DeviceInfoActivity.this, TripInfoActivity.class);
						activity.putExtra(TripInfoActivity.EXTRA_TRIP_INFO_OPCODE, ICD.RCMD_OPCODE_CTI);
						activity.putExtra(TripInfoActivity.EXTRA_UID, mDeviceUid);
						startActivity(activity);
						break;
					case COMMAND_SIS:
						if (D) Log.i(TAG, "SIS command chosen.");
						activity = new Intent(DeviceInfoActivity.this, TripStateActivity.class);
						activity.putExtra(TripInfoActivity.EXTRA_UID, mDeviceUid);
						startActivity(activity);
						break;
					case COMMAND_SL:
						if (D) Log.i(TAG, "SL command chosen.");
						getAndroNadService().sendLogCommand(mDeviceUid, ICD.RCMD_OPCODE_SL);
						break;
					case COMMAND_SLU:
						if (D) Log.i(TAG, "SLU command chosen.");
						getAndroNadService().sendLogCommand(mDeviceUid, ICD.RCMD_OPCODE_SLU);
						break;
					case COMMAND_EL:
						if (D) Log.i(TAG, "EL command chosen.");
						getAndroNadService().sendLogCommand(mDeviceUid, ICD.RCMD_OPCODE_EL);
						break;
					}
				}
			});
		
		return builder.create();
	}
	
	/**
	 * Print the device info to the screen, if available.
	 */
	private void displayDeviceInfo() {
        if (mDeviceInfo != null) {
        	mInfoTextView.setText(mDeviceInfo.toString());
        }
	}
	
	/**
	 * Update UI with latest info from the AndroNadService.
	 * 
	 * @param service
	 */
	private void updateInfo(AndroNadService service) {
		if (service != null) {
			DeviceInfo temp = service.getDeviceInfo(mDeviceUid);
			if (temp != null) {
				mDeviceInfo = temp;
				displayDeviceInfo();
			}
		}
	}

	@Override
	protected void onDataUpdated(AndroNadService service) {
		updateInfo(service);
	}

	@Override
	protected void onServiceConnected(AndroNadService service) {
		updateInfo(service);
	}	
}
