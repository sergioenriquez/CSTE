package cste.android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.hnad.EcocDevice;

public class ECoCInfoActivity extends HnadBaseActivity {
	private static final String TAG = "ECoC Info Activity";
	private EcocDevice device;
	
	TextView mDeviceUIDTxt;
	TextView mDeviceTypeTxt;
	TextView mSealIDTxt;
	TextView mManifestIDTxt;
	TextView mDeviceRSSITxt;
	TextView mAckNoTxt;
	
	Button mSetTimeBtn;
	Button mSetTripInfoBtn;
	Button mSetWaypointBtn;
	Button mDecommissioBtn;
	Button mResetAlarmBtn;
	Button mViewEventLogBtn;

	CheckBox mLockOpenBox;
	CheckBox mHaspOpenBox;
	CheckBox mOffCourseBox;
	CheckBox mOffScheduleBox;
	
	CheckBox mConfigMalfunctionBox;
	CheckBox mInsuficientPowerBox;
	CheckBox mCommisionFailedBox;
	CheckBox mTimeNotSetBox;
	CheckBox mSensorMalfunctionBox;
	CheckBox mDecryptionErrorBox;
	CheckBox mInvalidCommandBox;
	CheckBox mLogOverflowBox;
	CheckBox mAckFailureBox;
	CheckBox mConfigFailedBox;
	CheckBox mSensorEnableFailedBox;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecocdetails);
        
        device = getIntent().getParcelableExtra("device"); 
       
        mDeviceUIDTxt =  (TextView)findViewById(R.id.devUID);
        mDeviceTypeTxt = (TextView)findViewById(R.id.devType);
        mSealIDTxt = 	 (TextView)findViewById(R.id.sealID);
        mManifestIDTxt = (TextView)findViewById(R.id.manifestID);
        mDeviceRSSITxt = (TextView)findViewById(R.id.rssiVal);
        mAckNoTxt = 	 (TextView)findViewById(R.id.ackNo);
        
        mSetTimeBtn = 		(Button)findViewById(R.id.setTimeBtn);
        mSetTripInfoBtn = 	(Button)findViewById(R.id.setTripInfo);
        mSetWaypointBtn = 	(Button)findViewById(R.id.setWaypointBtn);
        mDecommissioBtn = 	(Button)findViewById(R.id.decommissionBtn);
        mResetAlarmBtn = 	(Button)findViewById(R.id.resetAlarmBtn);
        mViewEventLogBtn =  (Button)findViewById(R.id.viewEventLogBtn);

        mLockOpenBox =		(CheckBox)findViewById(R.id.lockOpenBox);
    	mHaspOpenBox =		(CheckBox)findViewById(R.id.haspOpenBox);
    	mOffCourseBox =		(CheckBox)findViewById(R.id.offCourseBox);
    	mOffScheduleBox =	(CheckBox)findViewById(R.id.offScheduleBox);
    	
    	mConfigMalfunctionBox =	(CheckBox)findViewById(R.id.configMalfunctionBox);
    	mInsuficientPowerBox =	(CheckBox)findViewById(R.id.insuficientPowerBox);
    	mCommisionFailedBox =	(CheckBox)findViewById(R.id.commisionFailedBox);
    	mTimeNotSetBox =		(CheckBox)findViewById(R.id.timeNotSetBox);
    	mSensorMalfunctionBox =	(CheckBox)findViewById(R.id.sensorMalfunctionBox);
    	mDecryptionErrorBox =	(CheckBox)findViewById(R.id.decryptionErrorBox);
    	mInvalidCommandBox =	(CheckBox)findViewById(R.id.invalidCommandBox);
    	mLogOverflowBox =		(CheckBox)findViewById(R.id.logOverflowBox);
    	mAckFailureBox =		(CheckBox)findViewById(R.id.ackFailureBox);
    	mConfigFailedBox =		(CheckBox)findViewById(R.id.configFailedBox);
    	mSensorEnableFailedBox =(CheckBox)findViewById(R.id.sensorEnableFailedBox);
        
    	setButtonActions();
        reloadDeviceData();//showProgressDialog();
	}
	
	protected void setButtonActions(){
		mSetTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.SET_TIME);
            }
        });
		mSetTripInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.SET_TRIPINFO);
            }
        });
		mSetWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.SET_WAYPOINTS);
            }
        });
		mDecommissioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.SET_COMMISION_OFF);
            }
        });
		mResetAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.SET_ALARM_OFF);
            }
        });
		mViewEventLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.CLEAR_EVENT_LOG);
            }
        });
	}

	protected void reloadDeviceData(){
		mDeviceUIDTxt.setText(device.UID().toString());

		mDeviceRSSITxt.setText("-" + String.valueOf(device.rssi) + " db");
      
	}

	@Override
	protected void onCoreServiceCBound()
	{
		mHnadCoreService.sendDevCmd(device.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {

		if ( data.containsKey(Events.DEVICE_INFO_CHANGED) ) {
			device = data.getParcelable("device");
			if(device == null)
				return;
			reloadDeviceData();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.device_info_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
//TODO CHANGE HTIS
        switch (item.getItemId()) {
            case R.id.settings:
            	startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
                break;
            case R.id.logout:
            	mHnadCoreService.logout();
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
}//end class
