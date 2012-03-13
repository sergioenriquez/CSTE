package cste.android.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import cste.android.R;

public class DeviceKeysActivity extends HnadBaseActivity {
	static final String TAG = "Keys screen";
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configlayout);

        IntentFilter filter = new IntentFilter();
        registerReceiver(mDeviceUpdateReceiver, filter); 
	}


	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onCoreServiceCBound() {
		// TODO Auto-generated method stub
		
	}
}

