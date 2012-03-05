package cste.android.activities;

import static cste.android.core.HNADService.Events.DEVLIST_CHANGED;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import cste.android.R;
import cste.android.core.HNADService.Events;
import cste.hnad.EcocDevice;

public class ECoCInfoActivity extends HnadBaseActivity {

	//private TextView devUID;
	private EcocDevice device;
	private ProgressDialog pd;
	
	TextView deviceUID;
	TextView deviceRSSI;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecocdetails);
        
        device = getIntent().getParcelableExtra("device"); 
        
        deviceUID = (TextView)findViewById(R.id.deviceUID);
        deviceRSSI = (TextView)findViewById(R.id.rssiVal);
        
        reloadDeviceData();
       //show progrss dialog
        //showProgressDialog();
	}
	
	protected void reloadDeviceData(){
		deviceUID.setText(device.UID().toString());

        deviceRSSI.setText("-" + String.valueOf(device.rssi) + " db");
	}
	
	private void showProgressDialog(){
		pd = ProgressDialog.show(this, "Working..", "Refreshing Device Information", true, true);
	}
	
	@Override
	protected void onCoreServiceCBound()
	{
		//refresh device status information
		mHnadCoreService.getDeviceStatus(device.UID());
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {

		if ( data.containsKey(Events.DEVICE_INFO_CHANGED) ) {
			device = data.getParcelable("device");
			if(device == null)
				return;
			reloadDeviceData();
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
//TODO CHANGE HTIS
        switch (item.getItemId()) {
            case R.id.settings:
            	startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
                break;
            case R.id.logout:
            	mHnadCoreService.logout();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            	finish();
                break;
            case R.id.eventlog:

                break;
                
            case R.id.upload:

                break;
                
            case R.id.viewtrip:

                break;
                
            case R.id.viewkeys:
                startActivity(new Intent(getApplicationContext(), DeviceKeysActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}//end class
