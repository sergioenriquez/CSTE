package cste.android.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import cste.android.R;

public class TripInfoActivity extends HnadBaseActivity{
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_info_layout);
        
        setWindowTitle(R.string.tripinfo_title);
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		// TODO Auto-generated method stub
	      IntentFilter filter = new IntentFilter();
	      registerReceiver(mDeviceUpdateReceiver, filter); 
	}

	@Override
	protected void onCoreServiceCBound() {
		// TODO Auto-generated method stub
		
	}
}
