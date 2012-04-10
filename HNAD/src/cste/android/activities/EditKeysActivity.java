package cste.android.activities;

import static cste.icd.general.Utility.hexToStr;
import static cste.icd.general.Utility.strToHex;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import cste.android.R;
import cste.android.core.HNADService.Events;
import cste.icd.components.ECoC;
import cste.icd.types.DeviceUID;
import cste.misc.HexKeyListener;

public class EditKeysActivity extends HnadBaseActivity {
	static final String TAG = "Edit keys activity";
	
	protected EditText tcktxt;
	protected EditText tckAscTxt;
	protected DeviceUID devUID;
	protected Button clearBtn;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecoc_edit_layout);
        setWindowTitle(R.string.keys_title);
        
        tcktxt = (EditText) findViewById(R.id.tckTxt);
        tcktxt.setFilters(new InputFilter[]{
   	         new InputFilter.LengthFilter(32),
   	         new InputFilter.AllCaps()
   	         });
        tcktxt.setKeyListener(new HexKeyListener());
        tckAscTxt = (EditText) findViewById(R.id.tckAscTxt);
        clearBtn = (Button) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	tcktxt.setText("00000000000000000000000000000000");
            	tckAscTxt.setText("0");
            	finish();
            }
        });
        
        devUID = (DeviceUID)getIntent().getSerializableExtra("deviceUID");

        IntentFilter filter = new IntentFilter();
		filter.addAction(Events.TRANSMISSION_RESULT);
		registerReceiver(mDeviceUpdateReceiver, filter); 
	}
	
	protected void saveKey(){	
		int asc = Integer.parseInt(tckAscTxt.getText().toString());
		byte []tck = strToHex(tcktxt.getText().toString());
		mHnadCoreService.setDeviceTCK(devUID,tck);
		mHnadCoreService.setDeviceAssensionVal(devUID, asc);
	}

	@Override
	protected void handleCoreServiceMsg(String action, Intent intent) {

	}

	@Override
	protected void onCoreServiceCBound() {
		ECoC eCoC = (ECoC) mHnadCoreService.getDeviceRecord(devUID);
		if(eCoC==null)
			return;
		tcktxt.setText( hexToStr(eCoC.getTCK()) );
		tckAscTxt.setText( String.valueOf(eCoC.txAscension) );
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	saveKey();
    }

}
