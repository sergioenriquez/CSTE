package cste.android.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import cste.android.R;

public class EventLogHNADActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Event Log Activity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);

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
