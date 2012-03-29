package cste.android.activities;

import android.content.Intent;
import android.os.Bundle;
import cste.android.R;

public class ECoCEditActivity extends HnadBaseActivity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecoc_edit_layout);

	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onCoreServiceCBound() {
		// TODO Auto-generated method stub
		
	}

}
