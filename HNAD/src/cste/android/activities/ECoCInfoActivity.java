package cste.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;
import cste.messages.RestrictedStatusECM;

public class ECoCInfoActivity extends HnadBaseActivity {
	private static final String TAG = "ECoC Info Activity";
	protected EcocDevice mECoCDev;
	
	TextView mDeviceUIDTxt;
	TextView mDeviceTypeTxt;
	TextView mConveyanceID;
	TextView mGpsLocation;
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
	
	//TODO move this to restricted event class
	static final byte BIT_0 = 0x01;
	static final byte BIT_1 = 0x02;
	static final byte BIT_2 = 0x04;
	static final byte BIT_3 = 0x08;
	static final byte BIT_4 = 0x10;
	static final byte BIT_5 = 0x20;
	static final byte BIT_6 = 0x40;
	static final byte BIT_7 = (byte)0x80;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecocdetails);

        mECoCDev = getIntent().getParcelableExtra("device"); 
       
        mDeviceUIDTxt =  (TextView)findViewById(R.id.devUID);
        mDeviceTypeTxt = (TextView)findViewById(R.id.devType);
        mConveyanceID = 	 (TextView)findViewById(R.id.conveyanceID);
        mGpsLocation = (TextView)findViewById(R.id.gpsLoc);
        mDeviceRSSITxt = (TextView)findViewById(R.id.rssiVal);
        mAckNoTxt = 	 (TextView)findViewById(R.id.ackNo);
        
//        mSetTimeBtn = 		(Button)findViewById(R.id.setTimeBtn);
//        mSetTripInfoBtn = 	(Button)findViewById(R.id.setTripInfo);
//        mSetWaypointBtn = 	(Button)findViewById(R.id.setWaypointBtn);
//        mDecommissioBtn = 	(Button)findViewById(R.id.decommissionBtn);
//        mResetAlarmBtn = 	(Button)findViewById(R.id.resetAlarmBtn);
//        mViewEventLogBtn =  (Button)findViewById(R.id.viewEventLogBtn);

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
        
    	//setButtonActions();
        //reloadDeviceData();//showProgressDialog();
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.DEVICE_INFO_CHANGED);
		filter.addAction(Events.TRANSMISSION_RESULT);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}

	protected void reloadDeviceData(){
		mECoCDev = (EcocDevice) mHnadCoreService.getDeviceRecord(mECoCDev.UID());
		mDeviceUIDTxt.setText(mECoCDev.UID().toString());
		mDeviceTypeTxt.setText(mECoCDev.devType().toString());
		mAckNoTxt.setText(String.valueOf(mECoCDev.rxAscension));
		mDeviceRSSITxt.setText("-" + String.valueOf(mECoCDev.rssi) + " db");

		RestrictedStatusECM status = (RestrictedStatusECM)mECoCDev.getRestrictedStatus();
		if( status == null)
			return;
		
		mConveyanceID.setText(status.getConveyanceStr());
		mGpsLocation.setText(status.getGpsStr());
		
		
		mLockOpenBox.setChecked( (status.alarmCode & BIT_7) > 0);
		mHaspOpenBox.setChecked( (status.alarmCode & BIT_6) > 0);
		mOffCourseBox.setChecked( (status.alarmCode & BIT_5) > 0);
		mOffScheduleBox.setChecked( (status.alarmCode & BIT_4) > 0);

		mTimeNotSetBox.setChecked( (status.errorCode & BIT_2) > 0);
		mCommisionFailedBox.setChecked( (status.errorCode & BIT_5) > 0);
		mInsuficientPowerBox.setChecked( (status.errorCode & BIT_6) > 0);
		mConfigMalfunctionBox.setChecked( (status.errorCode & BIT_7) > 0);
		
		mSensorMalfunctionBox.setChecked( (status.errorBits & BIT_7) > 0);
		mDecryptionErrorBox.setChecked( (status.errorBits & BIT_6) > 0);
		mInvalidCommandBox.setChecked( (status.errorBits & BIT_5) > 0);
		mLogOverflowBox.setChecked( (status.errorBits & BIT_4) > 0);
		mAckFailureBox.setChecked( (status.errorBits & BIT_3) > 0);
		mConfigFailedBox.setChecked( (status.errorBits & BIT_2) > 0);
		mSensorEnableFailedBox.setChecked( (status.errorBits & BIT_1) > 0);
	}

	@Override
	protected void onCoreServiceCBound(){
		//mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
		//showProgressDialog("Requesting Device Information");
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		DeviceUID devUID = (DeviceUID)intent.getSerializableExtra("deviceUID");
		if( devUID == null || !devUID.equals(mECoCDev.UID())){
			return;
		}
		pd.cancel();
		if ( action.equals(Events.DEVICE_INFO_CHANGED)  ) {
			reloadDeviceData();
		}
		
		if ( action.equals(Events.TRANSMISSION_RESULT )) {
			boolean result = intent.getBooleanExtra("result",false);
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
        switch (item.getItemId()) {
        case R.id.refresh:
        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
    		showProgressDialog("Requesting Device Information");
        	return true;
        case R.id.viewEventLog:
        	//mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_EVENT_LOG);
    		//showProgressDialog("Requesting Event Log");
    		// do this on event log screen
    		Intent intent = new Intent(getApplicationContext(), EventLogECMActivity.class);
    		intent.putExtra("device", (Parcelable) mECoCDev);
    		
    		startActivity(intent);
            return true;
        case R.id.clearAlarm:

            return true;
       
        case R.id.setTrip:

            return true;
            
        case R.id.commission:

            return true;
        case R.id.erase:
        	mHnadCoreService.deleteDeviceRecord(this.mECoCDev.UID());
        	finish();
            return true;
        case R.id.assn:
        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
        	input = new EditText(this);
        	input.setInputType(InputType.TYPE_CLASS_NUMBER);
        	input.setText(Integer.toString(mECoCDev.txAscension));
        	alert.setView(input);
        	alert.setTitle("Enter the new tx assension val");
        	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        		  String value = input.getText().toString();
        		  int val = Integer.valueOf(value);
        		  mHnadCoreService.setDeviceAssensionVal(mECoCDev.UID(), val);
        		  }
        		});
        	alert.show();
        	//info.position
            return true;
        case R.id.setTime:
        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_TIME);
        	showProgressDialog("Setting time");
        	return true;
        case R.id.clearLog:
        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.CLEAR_EVENT_LOG);
        	showProgressDialog("Clearing log");
        	return true;
        }
    	
        return super.onOptionsItemSelected(item);
    }
	
	EditText input;

	
	private void showProgressDialog(String msg){
		pd.setMessage(msg);
		pd.show();
	}

}//end class




//@Override
//protected void onResume(){
//	super.onResume();
//}

//protected void setButtonActions(){
//	mSetTimeBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_TIME);
//        }
//    });
//	mSetTripInfoBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_TRIPINFO);
//        }
//    });
//	mSetWaypointBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_WAYPOINTS);
//        }
//    });
//	mDecommissioBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_COMMISION_OFF);
//        }
//    });
//	mResetAlarmBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.SET_ALARM_OFF);
//        }
//    });
//	mViewEventLogBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.CLEAR_EVENT_LOG);
//        }
//    });
//}
