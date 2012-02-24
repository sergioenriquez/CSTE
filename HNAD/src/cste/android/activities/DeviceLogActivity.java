package cste.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class DeviceLogActivity extends HnadBaseActivity {
	static final String TAG = "Device Event Log Activity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textview = new TextView(this);
        textview.setText("This is the log tab");
        setContentView(textview);
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {
		// TODO Auto-generated method stub
		
	}
}
