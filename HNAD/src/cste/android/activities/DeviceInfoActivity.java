package cste.android.activities;

import static cste.android.core.HnadCoreService.Events.DEVLIST_CHANGED;
import static cste.android.core.HnadCoreService.Events.USB_STATE_CHANGED;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import cste.android.R;
import cste.android.core.HnadCoreService;

public class DeviceInfoActivity extends Activity {

	private HnadCoreService mHnadCoreService = null;
	private boolean mIsBound = false;
	private TextView devUID;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        devUID = (TextView)findViewById(R.id.devUID);
        
        TextView textview = new TextView(this);
        textview.setText("This is the info tab");
        setContentView(R.layout.devinfotab);
        
        // TODO this should be part of the overclass
        this.registerReceiver(mDeviceUpdateReceiver, new IntentFilter(HnadCoreService.Events.HNAD_CORE_EVENT_MSG));
        doBindService();
	}
	
	private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			

			
			if ( intent.hasExtra(USB_STATE_CHANGED))
			{

			}
		}
	};	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mHnadCoreService = ((HnadCoreService.LocalBinder)service).getService();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mHnadCoreService = null;
		}
	};
	
	void doBindService() {
		bindService(new Intent(this,HnadCoreService.class), mConnection, Context.BIND_AUTO_CREATE);    
		mIsBound = true;
	}
	
	void doUnbindService() {    
		if (mIsBound) {
			unbindService(mConnection);        
			mIsBound = false;    
		}
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(mDeviceUpdateReceiver); 
		super.onDestroy();
	}
}//end class
