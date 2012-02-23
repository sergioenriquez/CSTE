package cste.android.activities;

import static cste.android.core.HnadCoreService.Events.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.android.future.usb.UsbAccessory;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cste.android.R;
import cste.android.core.HnadCoreService;
import cste.misc.ZigbeeAPI;

/***
 * Lets user enter their login credentials to authenticated with the DCP server. If successful it will then launch the device details activity.
 * @author User
 *
 */
public class LoginActivity extends Activity {
	static final String TAG = "HNAD Login";
	
	private HnadCoreService mHnadCoreService = null;
	private Button loginButton;
	private boolean mIsBound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.login);
        loginButton = (Button)findViewById(R.id.authLoginBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
                startActivity(intent);
                finish();
            }
        });
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(HNAD_CORE_EVENT_MSG);
        this.registerReceiver(mDeviceUpdateReceiver, filter); 
        
        doBindService();
        
       
    }

    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
  
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
            	Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(intent);
                mHnadCoreService.uploadData(); //TODO just a test
       
                break;
            case R.id.exit:
            	this.stopService(new Intent("cste.android.core.HNADCORESERVICE"));
            	finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
	
	private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Toast.makeText(getApplicationContext(), String.valueOf(intent.getIntExtra("itemChanged", 0)), Toast.LENGTH_SHORT).show();
			if( action.equals(LOGIN_RESULT)){
				
			}

//			if (DEVICES_UPDATED.equals("")) {
//				//TODO
//				TextView tv = (TextView)findViewById(R.id.debugText);
//				tv.setText("Sensor: " + String.valueOf(intent.getIntExtra("itemChanged", 0)));
//				//Toast.makeText(getApplicationContext(), String.valueOf(intent.getIntExtra("itemChanged", 0)), Toast.LENGTH_SHORT).show();
//				
//			}
		}
	};

    @Override
	public Object onRetainNonConfigurationInstance() {
			return super.onRetainNonConfigurationInstance();
	}
    
    @Override
	public void onDestroy() {
    	unregisterReceiver(mDeviceUpdateReceiver);
    	doUnbindService();
		super.onDestroy();
	}
    
    @Override
	public void onResume() {
		super.onResume();
	}
    
    private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mHnadCoreService = ((HnadCoreService.LocalBinder)service).getService();
			//Toast.makeText(getApplicationContext(), "Login - Service bound", Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mHnadCoreService = null;
		}
	};
	
	private void doBindService() {
		bindService(new Intent(this,HnadCoreService.class), mConnection, Context.BIND_AUTO_CREATE);    
		mIsBound = true;
	}
	
	private void doUnbindService() {    
		if (mIsBound) {
			unbindService(mConnection);        
			mIsBound = false;    
		}
	}
}