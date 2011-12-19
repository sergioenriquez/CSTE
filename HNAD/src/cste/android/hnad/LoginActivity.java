package cste.android.hnad;

import static cste.android.hnad.HnadCoreService.DEVICES_UPDATED;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.android.future.usb.UsbAccessory;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import cste.android.usb.UsbCommHandler;

public class LoginActivity extends Activity {
	static final String TAG = "HNAD Login";
	
	private Button loginButton;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login);
        
        loginButton = (Button)findViewById(R.id.authLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), DeviceListActivity.class);
            	//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        
        IntentFilter filter = new IntentFilter();
		filter.addAction(HnadCoreService.DEVICES_UPDATED);
        this.registerReceiver(mDeviceUpdateReceiver, filter); 
    }

    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // Intent intent;
        switch (item.getItemId()) {
            case R.id.settings:
            	Intent intent = new Intent(this, DeviceListActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	
    @Override
	public Object onRetainNonConfigurationInstance() {
			return super.onRetainNonConfigurationInstance();
	}
    
    @Override
	public void onDestroy() {

		super.onDestroy();
	}
    
    @Override
	public void onResume() {
		super.onResume();

	}
    
    private final BroadcastReceiver mDeviceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DEVICES_UPDATED.equals(action)) {
				//TODO
				TextView tv = (TextView)findViewById(R.id.debugText);
				tv.setText("Sensor: " + String.valueOf(intent.getIntExtra("itemChanged", 0)));
				//Toast.makeText(getApplicationContext(), String.valueOf(intent.getIntExtra("itemChanged", 0)), Toast.LENGTH_SHORT).show();
				
			}
		}
	};

}