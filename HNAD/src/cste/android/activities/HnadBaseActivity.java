/**
 * 
 */
package cste.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import cste.android.core.HNADService;
import static cste.android.core.HNADService.Events.*;

/**
 * @author enriquez
 *
 */
@SuppressWarnings("unused")
public abstract class HnadBaseActivity extends Activity{
	protected HNADService mHnadCoreService = null;
	protected boolean mIsBound = false;
	protected ProgressDialog pd; 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pd = new ProgressDialog(this);
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
	
	abstract protected void handleCoreServiceMsg(String action,Intent intent);
	
	protected abstract void onCoreServiceCBound();
	
	protected final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			handleCoreServiceMsg(intent.getAction(), intent);
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

