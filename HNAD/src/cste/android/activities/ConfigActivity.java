package cste.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import cste.android.R;
import cste.android.core.HNADService.SettingsKey;

public class ConfigActivity extends HnadBaseActivity {
	EditText thisUIDText;
	EditText ftpAddress;
	EditText dcpAddress;
	EditText dcpUID;
	CheckBox useEncryption;
	Spinner nadaBurst;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configlayout);
        
        thisUIDText = 		(EditText)findViewById(R.id.thisUID);
        ftpAddress = 	(EditText)findViewById(R.id.ftpServer);
        dcpAddress = 	(EditText)findViewById(R.id.dcpServer);
        dcpUID = 		(EditText)findViewById(R.id.dcpUID);
        useEncryption = (CheckBox)findViewById(R.id.ecryptionOn);
        nadaBurst = 	(Spinner)findViewById(R.id.nadaRate);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.nada_timing, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nadaBurst.setAdapter(adapter);
        
        IntentFilter filter = new IntentFilter();
        registerReceiver(mDeviceUpdateReceiver, filter); 
	}
	
	@Override
    protected void onCoreServiceCBound(){
    	//load saved settings
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();

    	thisUIDText.setText( settings.getString(SettingsKey.THIS_UID, "0013A20040715FD8") );
    	ftpAddress.setText( settings.getString(SettingsKey.FTP_ADDR, "attila.sdsu.edu") );
    	dcpAddress.setText( settings.getString(SettingsKey.DCP_ADDR, "192.168.1.1") );
    	dcpUID.setText( settings.getString(SettingsKey.DCP_UID, "0000000000000000") );
    	useEncryption.setChecked( settings.getBoolean(SettingsKey.USE_ENC, false) );
    	nadaBurst.setSelection( settings.getInt(SettingsKey.NADA_BURST, 0) );
	}
	
	@Override
    protected void onStop() {
		super.onStop();
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();
    	SharedPreferences.Editor editor = settings.edit();

    	editor.putString(SettingsKey.THIS_UID, thisUIDText.getText().toString());
    	editor.putString(SettingsKey.FTP_ADDR, ftpAddress.getText().toString());
    	editor.putString(SettingsKey.DCP_ADDR, dcpAddress.getText().toString());
    	editor.putString(SettingsKey.DCP_UID, dcpUID.getText().toString());
    	editor.putBoolean(SettingsKey.USE_ENC, useEncryption.isChecked());
    	editor.putInt(SettingsKey.NADA_BURST, nadaBurst.getSelectedItemPosition());
    	editor.commit();
    }

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		
	}
}
