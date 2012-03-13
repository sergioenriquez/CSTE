package cste.android.activities;

import cste.android.R;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class EventLogHNADActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Event Log Activity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configlayout);

        IntentFilter filter = new IntentFilter();
        registerReceiver(mDeviceUpdateReceiver, filter); 
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		
	}

	@Override
	protected void onCoreServiceCBound() {
		// TODO Auto-generated method stub
		
	}
}
