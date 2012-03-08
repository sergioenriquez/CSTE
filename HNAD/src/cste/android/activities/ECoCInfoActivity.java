package cste.android.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import cste.android.R;
import cste.android.core.HNADService.DeviceCommands;
import cste.android.core.HNADService.Events;
import cste.components.ComModule;
import cste.hnad.EcocDevice;
import cste.icd.DeviceUID;
import cste.messages.ECoCRestrictedStatus;
import static cste.icd.Utility.strToHex;
import static cste.icd.Utility.hexToStr;;

public class ECoCInfoActivity extends HnadBaseActivity {
	private static final String TAG = "ECoC Info Activity";
	private EcocDevice mECoCDev;
	private ProgressDialog pd;
	
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
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecocdetails);
        pd = new ProgressDialog(this);
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
        reloadDeviceData();//showProgressDialog();
	}


	protected void reloadDeviceData(){
		
		mDeviceUIDTxt.setText(mECoCDev.UID().toString());
		mDeviceTypeTxt.setText(mECoCDev.devType().toString());
		mAckNoTxt.setText(String.valueOf(mECoCDev.rxAscension));
		mDeviceRSSITxt.setText("-" + String.valueOf(mECoCDev.rssi) + " db");

		ECoCRestrictedStatus status = (ECoCRestrictedStatus)mECoCDev.getRestrictedStatus();
		if( status == null)
			return;
		
		mConveyanceID.setText(hexToStr(status.coveyanceID));
		mGpsLocation.setText(hexToStr(status.gpsLoc));
		
		
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
		mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_RESTRICTED_STATUS);
		//showProgressDialog("Requesting Device Information");
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {

		if ( data.containsKey(Events.DEVICE_INFO_CHANGED) ) {
			DeviceUID devUID = (DeviceUID)data.getSerializable("deviceUID");
			if( devUID != null && devUID.equals(mECoCDev.UID())){
				EcocDevice dev = (EcocDevice)mHnadCoreService.getDeviceRecord(devUID);
				if(dev == null) return;
				mECoCDev = dev;
				reloadDeviceData();
			}
		}
		
		if ( data.containsKey(Events.TRANSMISSION_RESULT) ) {
			DeviceUID devUID = (DeviceUID)data.getSerializable("deviceUID");
			boolean result = data.getBoolean("result");

			pd.cancel();
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
    		//showProgressDialog("Requesting Device Information");
        	return true;
        case R.id.viewEventLog:
        	mHnadCoreService.sendDevCmd(mECoCDev.UID(),DeviceCommands.GET_EVENT_LOG);
    		//showProgressDialog("Requesting Event Log");
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
        }
        return super.onOptionsItemSelected(item);
    }
	
	EditText input;

	
	private void showProgressDialog(String msg){
		pd = ProgressDialog.show(this, "Working..", msg , true, true);
	}
	
	static final byte BIT_0 = 0x01;
	static final byte BIT_1 = 0x02;
	static final byte BIT_2 = 0x04;
	static final byte BIT_3 = 0x08;
	static final byte BIT_4 = 0x10;
	static final byte BIT_5 = 0x20;
	static final byte BIT_6 = 0x40;
	static final byte BIT_7 = (byte)0x80;

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
