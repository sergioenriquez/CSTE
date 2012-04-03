package hnad.android.Activity;

import hnad.android.R;
import hnad.android.ICD.ICD;
import hnad.android.Service.AndroNadService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


public class SettingsActivity extends AndroNadActivity {
	// For debugging
	private static final String TAG = SettingsActivity.class.getName();
	private static final boolean D = true;
	
	// layout items
	private CheckBox checkBoxUseEncryption;
	private EditText editTextKey;
	private Button	 buttonSaveKey;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up activity name in custom title
        setLeftTitle(R.string.settings);
        
        // set up main layout
        setContentView(R.layout.settings);
        checkBoxUseEncryption 	= (CheckBox) findViewById(R.id.checkbox_enc_enable);
        editTextKey 			= (EditText) findViewById(R.id.et_enc_key);
        buttonSaveKey			= (Button) findViewById(R.id.button_save_key);
        buttonSaveKey.setOnClickListener(saveButtonOnClickListener);
        checkBoxUseEncryption.setOnCheckedChangeListener(useEncryptionCheckedListener);
    }
    
    private final CompoundButton.OnCheckedChangeListener useEncryptionCheckedListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (getAndroNadService() != null) {
				getAndroNadService().setEncryption(isChecked);
			} else {
				Toast.makeText(SettingsActivity.this, R.string.toast_disconnected_error, Toast.LENGTH_SHORT).show();
			}
		}
	};
    
    private final View.OnClickListener saveButtonOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (getAndroNadService() != null) {
				if (editTextKey.length() == ICD.ENCRYPTION_KEY_LENGTH_IN_HEX) {
					getAndroNadService().setEncryptionKey(editTextKey.getText().toString());
					Toast.makeText(SettingsActivity.this, R.string.toast_key_changed, Toast.LENGTH_SHORT).show();
				}
				else {
					String msg = getResources().getString(R.string.toast_key_too_short).replace("<length", Integer.toString(ICD.ENCRYPTION_KEY_LENGTH_IN_HEX));
					Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(SettingsActivity.this, R.string.toast_disconnected_error, Toast.LENGTH_SHORT).show();
			}
		}
	};
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {
        case R.id.close:
        	finish();
        	return true;
        }
        return false;
    }
	
	@Override
	protected void onServiceConnected(AndroNadService service) {
		// get current settings from service
		if (service != null) {
			editTextKey.setText(service.getEncryptionKey());
			checkBoxUseEncryption.setChecked(service.useEncryption());
			
			checkBoxUseEncryption.setEnabled(true);
			editTextKey.setEnabled(true);
			buttonSaveKey.setEnabled(true);
		}
	}

	@Override
	protected void onServiceDisconnected() {
		checkBoxUseEncryption.setEnabled(false);
		editTextKey.setEnabled(false);
		buttonSaveKey.setEnabled(false);
	}
	
	
}
