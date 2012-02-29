package cste.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import cste.android.R;
import cste.hnad.Device;

public class DeviceDetailsActivity extends HnadBaseActivity {
	static final String TAG = "Device Info";
	
	TextView devUID;
	TextView devType;
	TextView devSealID;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devinfotab);
        
        Device dev = getIntent().getParcelableExtra("device");
        
//        devUID = (TextView)findViewById(R.id.devUID);
//        devType = (TextView)findViewById(R.id.devType);
//        devSealID = (TextView)findViewById(R.id.devSeal);
//        
//        devUID.setText(dev.devUID.toString());
//        devType.setText(dev.devType.toString());
//        devSealID.setText(dev.sealID);
	}
	@Override
	protected void onCoreServiceCBound()
	{
		//mHnadCoreService
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {

		
	}
}//end class
