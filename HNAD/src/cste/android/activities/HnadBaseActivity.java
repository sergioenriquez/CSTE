/**
 * 
 */
package cste.android.activities;

import static cste.android.core.HNADService.Events.HNAD_CORE_EVENT_MSG;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import cste.android.core.HNADService;

/**
 * @author enriquez
 *
 */
public abstract class HnadBaseActivity extends Activity{
	protected HNADService mHnadCoreService = null;
	protected boolean mIsBound = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerReceiver(mDeviceUpdateReceiver, new IntentFilter(HNAD_CORE_EVENT_MSG)); 
        doBindService();
    }
	
	@Override
	public void onDestroy() {
    	unregisterReceiver(mDeviceUpdateReceiver);
    	doUnbindService();
		super.onDestroy();
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mHnadCoreService = ((HNADService.LocalBinder)service).getService();
			mIsBound = true;
			onCoreServiceCBound();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mHnadCoreService = null;
		}
	};
	
	abstract protected void handleCoreServiceMsg(Context context,Bundle data);
	
	protected void onCoreServiceCBound()
	{
		
	}
	
	private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			handleCoreServiceMsg(context, intent.getExtras());
		}
	};

	private void doBindService() {
		bindService(new Intent(this,HNADService.class), mConnection, Context.BIND_AUTO_CREATE);    
	}
	
	private void doUnbindService() {    
		if (mIsBound) {
			unbindService(mConnection);        
			mIsBound = false;    
		}
	}
};

