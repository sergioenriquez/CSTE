package cste.android.activities;

import cste.android.R;
import android.content.Intent;
import android.os.Bundle;

public class KeyViewActivity extends HnadBaseActivity{
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keys_layout);

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
