package cste.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/***
 * This activity is started by connecting the USB accesory or by 
 * launching the application. If an accesory is detected, a message will be
 * passed to the core service so it is opened by the USB handler right away.
 * @author User
 *
 */
public class LauncherActivity extends Activity {
	static final String TAG = "HNAD Launcher";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = new Intent();
		i.setAction("cste.android.core.HNADCORESERVICE");
		
		UsbAccessory mAccessory = UsbManager.getAccessory(getIntent());
        if( mAccessory != null)
        	i.putExtra("usbAccesory", mAccessory.getDescription());

		startService(i);

		finish();
	}
}
