package cste.android.hnad;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class LauncherActivity extends Activity {
	static final String TAG = "Launcher";
	private HnadCoreService mBoundService;
	private boolean mIsBound;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((HnadCoreService.LocalBinder)service).getService();
			if ( mAccessory != null)
				mBoundService.getUsbHandler().openUsbAccesory(mAccessory);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
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
	protected void onDestroy() {
		super.onDestroy();    
		doUnbindService();
	}
	
	private UsbAccessory mAccessory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAccessory = UsbManager.getAccessory(getIntent());

		Toast toast = Toast.makeText(getApplicationContext(), "Launcher called", Toast.LENGTH_SHORT);
        toast.show();
        
		startService(new Intent(this,HnadCoreService.class));
		doBindService();
		
		finish();
	}
}