package cste.android.usb;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.future.usb.*;
/* This Activity does nothing but receive USB_DEVICE_ATTACHED events from the
 * USB service and then start the main HNAD activity
 */

public class UsbAccessoryActivity extends Activity {
	static final String TAG = "UsbAccessoryActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent usbIntent = getIntent();
		UsbAccessory accessory = UsbManager.getAccessory(usbIntent);

		Intent launcherIntent = new Intent(this, LauncherActivity.class);
		//launcherIntent.putExtra("", accessory);
		launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		try {
			startActivity(launcherIntent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "unable to start HNAD activity", e);
		}
		
		finish();
	}
}