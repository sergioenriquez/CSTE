package cste.android.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.System;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import cste.android.R;
import cste.android.core.HNADService.SettingsKey;


public class ConfigActivity extends HnadBaseActivity {
	static final String TAG = "Config activity";
	
	protected EditText 	mThisUidText;
	protected EditText 	mFtpAddress;
	protected EditText 	mDcpAddress;
	protected EditText 	mDcpUid;
	protected CheckBox 	mUseEncryption;
	protected Spinner 	mNadaBurst;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);
        setWindowTitle(R.string.settings);
        
        mThisUidText = 	(EditText)findViewById(R.id.thisUID);
        mFtpAddress = 	(EditText)findViewById(R.id.ftpServer);
        mDcpAddress = 	(EditText)findViewById(R.id.dcpServer);
        mDcpUid = 		(EditText)findViewById(R.id.dcpUID);
        mUseEncryption = (CheckBox)findViewById(R.id.ecryptionOn);
        mNadaBurst = 	(Spinner)findViewById(R.id.nadaRate);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.nada_timing, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNadaBurst.setAdapter(adapter);

        IntentFilter filter = new IntentFilter();
        registerReceiver(mDeviceUpdateReceiver, filter); 
	}
	
	@Override
    protected void onCoreServiceCBound(){
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();
    	String Android_ID = System.getString(this.getContentResolver(), System.ANDROID_ID).toUpperCase();
    	mThisUidText.setText( settings.getString(SettingsKey.THIS_UID, Android_ID ) );
    	mFtpAddress.setText( settings.getString(SettingsKey.FTP_ADDR, "attila.sdsu.edu") );
    	mDcpAddress.setText( settings.getString(SettingsKey.DCP_ADDR, "192.168.1.1") );
    	mDcpUid.setText( settings.getString(SettingsKey.DCP_UID, "0000000000000000") );
    	mUseEncryption.setChecked( settings.getBoolean(SettingsKey.USE_ENC, false) );
    	mNadaBurst.setSelection( settings.getInt(SettingsKey.NADA_BURST, 0) );
	}
	
	@Override
    protected void onStop() {
		super.onStop();
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();
    	SharedPreferences.Editor editor = settings.edit();

    	editor.putString(SettingsKey.THIS_UID, mThisUidText.getText().toString());
    	editor.putString(SettingsKey.FTP_ADDR, mFtpAddress.getText().toString());
    	editor.putString(SettingsKey.DCP_ADDR, mDcpAddress.getText().toString());
    	editor.putString(SettingsKey.DCP_UID, mDcpUid.getText().toString());
    	editor.putBoolean(SettingsKey.USE_ENC, mUseEncryption.isChecked());
    	editor.putInt(SettingsKey.NADA_BURST, mNadaBurst.getSelectedItemPosition());
    	editor.commit();
    	
    	mHnadCoreService.reloadSettings();
    }

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		
	}
}
