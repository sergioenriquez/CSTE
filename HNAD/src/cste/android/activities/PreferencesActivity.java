package cste.android.activities;

import android.content.Context;
import android.os.Bundle;

public class PreferencesActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Preferences";
	
	public static final String PREFERENCES_FILE = "MyPrefsFile";
	public static final String SERVER_ADDRESS = "DcpServerAddress";
	public static final String SERVER_PORT = "DcpServerPort";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO Preferences screen
        // setContentView(R.layout.devicelist);

    }
	
	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {
		// TODO Auto-generated method stub
		
	}

}
