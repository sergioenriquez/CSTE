package cste.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.components.ECoC;
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;
import cste.messages.RestrictedStatusECM;

public class ECoCInfoActivity extends HnadBaseActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "ECoC Info Activity";
	protected DeviceUID devUID;
	
	TextView mDeviceUIDTxt;
	TextView mDeviceTypeTxt;
	TextView mConveyanceID;
	TextView mGpsLocation;
	TextView mDeviceRSSITxt;
	TextView mRxAckTxt;
	TextView mTxAckTxt;

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
        setContentView(R.layout.ecoc_details_layout);

        devUID = (DeviceUID)getIntent().getSerializableExtra("deviceUID");
       
        mDeviceUIDTxt =  	(TextView)findViewById(R.id.devUID);
        mDeviceTypeTxt = 	(TextView)findViewById(R.id.devType);
        mConveyanceID = 	(TextView)findViewById(R.id.conveyanceID);
        mGpsLocation = 		(TextView)findViewById(R.id.gpsLoc);
        mDeviceRSSITxt = 	(TextView)findViewById(R.id.rssiVal);
        mRxAckTxt = 	 	(TextView)findViewById(R.id.ackNo);
        mTxAckTxt = 	 	(TextView)findViewById(R.id.txAckNo);

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

    	setWindowTitle(R.string.devdetails_title);
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.DEVICE_INFO_CHANGED);
		filter.addAction(Events.TRANSMISSION_RESULT);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}

	protected void reloadDeviceData(){
		ECoC eCoC = (ECoC) mHnadCoreService.getDeviceRecord(devUID);
		if(eCoC == null)
			return;
		
		mDeviceUIDTxt.setText(devUID.toString());
		mDeviceTypeTxt.setText(eCoC.devType().toString());
		mRxAckTxt.setText(String.valueOf(eCoC.rxAscension));
		mTxAckTxt.setText(String.valueOf(eCoC.txAscension));
		mDeviceRSSITxt.setText("-" + String.valueOf(eCoC.rssi) + " db");

		mConveyanceID.setText(eCoC.getConveyanceID().toString());
		mGpsLocation.setText(eCoC.getGpsLocation().toString());

		mLockOpenBox.setChecked( eCoC.lockOpen());
		mHaspOpenBox.setChecked( eCoC.haspOpen());
		mOffCourseBox.setChecked( eCoC.offCourse() );
		mOffScheduleBox.setChecked( eCoC.offSchedule());

		mTimeNotSetBox.setChecked( eCoC.timeNotSet());
		mCommisionFailedBox.setChecked( eCoC.commFailed());
		mInsuficientPowerBox.setChecked( eCoC.insuficientPower());
		mConfigMalfunctionBox.setChecked( eCoC.configMalfunction());
		
		mSensorMalfunctionBox.setChecked( eCoC.sensorMalfunction());
		mDecryptionErrorBox.setChecked( eCoC.decryptionError());
		mInvalidCommandBox.setChecked( eCoC.invalidCommand());
		mLogOverflowBox.setChecked(eCoC.logOverflow());
		mAckFailureBox.setChecked( eCoC.ackFailure());
		mConfigFailedBox.setChecked( eCoC.configFailure());
		mSensorEnableFailedBox.setChecked( eCoC.sensorEnableFailure());
	}

	@Override
	protected void onCoreServiceCBound(){
		reloadDeviceData();
		//mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
		//showProgressDialog("Requesting Device Information");
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		DeviceUID changedDevUID = (DeviceUID)intent.getSerializableExtra("deviceUID");
		if( changedDevUID == null || !changedDevUID.equals(devUID)){
			return;
		}
		pd.cancel();
		
		if ( action.equals(Events.DEVICE_INFO_CHANGED)  ) {
			reloadDeviceData();
		}
		
		if ( action.equals(Events.TRANSMISSION_RESULT )) {
			pd.cancel();
			//boolean result = intent.getBooleanExtra("result",false);
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
		Intent intent;
        switch (item.getItemId()) {
        case R.id.refresh:
        	mHnadCoreService.sendDevCmd(devUID,DeviceCommands.GET_RESTRICTED_STATUS);
    		showProgressDialog("Requesting Device Information");
        	return true;
        case R.id.viewEventLog:
    		intent = new Intent(getApplicationContext(), EventLogECMActivity.class);
    		intent.putExtra("deviceUID", devUID);
    		startActivity(intent);
            return true;
        case R.id.clearAlarm:
        	showProgressDialog("Clearing alarm...");
            return true;
        case R.id.commission:
        	showProgressDialog("Commissioning...");
            return true;
        case R.id.erase:
        	mHnadCoreService.deleteDeviceRecord(devUID);
        	finish();
            return true;
        case R.id.assn:
        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
        	ECoC eCoC = (ECoC) mHnadCoreService.getDeviceRecord(devUID);
        	if(eCoC==null)
        		return true;
        	input = new EditText(this);
        	input.setInputType(InputType.TYPE_CLASS_NUMBER);
        	input.setText(Integer.toString(eCoC.txAscension));
        	input.requestFocus();
        	alert.setView(input);
        	alert.setTitle("Enter the new tx assension val");
        	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        		  String value = input.getText().toString();
        		  int val = Integer.valueOf(value);
        		  mHnadCoreService.setDeviceAssensionVal(devUID, val);
        		}
        	});
        	alert.show();
            return true;
        case R.id.setTime:
        	mHnadCoreService.sendDevCmd(devUID,DeviceCommands.SET_TIME);
        	showProgressDialog("Setting time...");
        	return true;
        case R.id.clearLog:
        	mHnadCoreService.sendDevCmd(devUID,DeviceCommands.CLEAR_EVENT_LOG);
        	showProgressDialog("Clearing log...");
        	return true;
        }
    	
        return super.onOptionsItemSelected(item);
    }
	
	EditText input;


}//end class
