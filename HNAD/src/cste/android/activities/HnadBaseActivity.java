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
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cste.android.core.HNADService;
import cste.android.core.HNADService.Events;
import cste.android.R;
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
	protected TextView 	windowTitle;
    protected ImageView windowIcon;
    protected Resources res;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pd = new ProgressDialog(this);
        res = getResources();
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.window_title);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        
        windowTitle = (TextView) findViewById(R.id.title);
        windowIcon  = (ImageView) findViewById(R.id.icon);

        doBindService();
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.USB_STATE_CHANGED);
		registerReceiver(mDeviceUpdateReceiver, filter); 
    }
	
	protected void setWindowTitle(int resourceID){
		windowTitle.setText(res.getString(resourceID));
	}
	
	@Override
	public void onDestroy() {
    	unregisterReceiver(mDeviceUpdateReceiver);
    	doUnbindService();
		super.onDestroy();
	}
	
	protected void showProgressDialog(String msg){
		pd.setMessage(msg);
		pd.show();
	}
	
	protected void toast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void setUsbIcon(boolean state){
		if( state )
			windowIcon.setImageResource(R.drawable.usb_on);
		else
			windowIcon.setImageResource(R.drawable.usb_off);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mHnadCoreService = ((HNADService.LocalBinder)service).getService();
			mIsBound = true;
			
			setUsbIcon( mHnadCoreService.getUsbState()  );
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
			if ( intent.getAction().equals(Events.USB_STATE_CHANGED) ) {
				boolean state = intent.getBooleanExtra("usbState", false);
				setUsbIcon(state);
			}else
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

