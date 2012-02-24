package cste.android.activities;

import android.content.Context;
import android.os.Bundle;
import cste.android.R;

public class DeviceCmdActivity extends HnadBaseActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.devcmdtab);
	}

	@Override
	protected void handleCoreServiceMsg(Context context, Bundle data) {
		// TODO Auto-generated method stub
		
	}
}
