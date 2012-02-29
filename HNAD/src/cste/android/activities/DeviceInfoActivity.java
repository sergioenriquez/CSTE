package cste.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import cste.android.R;

public class DeviceInfoActivity extends HnadBaseActivity {

	//private TextView devUID;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //devUID = (TextView)findViewById(R.id.devUID);
        
        TextView textview = new TextView(this);
        textview.setText("This is the info tab");
        setContentView(R.layout.devinfotab);
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {
		// TODO Auto-generated method stub
		
	}
}//end class
