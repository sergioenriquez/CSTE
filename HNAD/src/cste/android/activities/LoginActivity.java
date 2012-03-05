package cste.android.activities;

import static cste.android.core.HNADService.Events.LOGIN_RESULT;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import cste.android.R;
import cste.android.core.HNADService.SettingsKey;

/***
 * Lets user enter their login credentials to authenticated with the DCP server. If successful it will then launch the device details activity.
 * @author User
 *
 */
public class LoginActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Login";

	private EditText usernameText;
	private EditText passwordText;
	private CheckBox rememberLoginBox;
	private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.login);
        
        usernameText = (EditText)findViewById(R.id.authUserLink);
        passwordText = (EditText)findViewById(R.id.authPassword);
        loginButton = (Button)findViewById(R.id.authLoginBtn);
        rememberLoginBox = (CheckBox)findViewById(R.id.remember);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//TODO show progress bar
            	mHnadCoreService.login("username", "password");
            }
        });
    }
    
    @Override
    protected void onCoreServiceCBound()
	{
    	//load saved settings
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();
    	String username = settings.getString(SettingsKey.USERNAME, "user");
    	String password = settings.getString(SettingsKey.PASSWORD, "");
    	boolean rememberPassword = settings.getBoolean(SettingsKey.REMEMBER_PASS, false);
    	
    	usernameText.setText(username);
    	passwordText.setText(password);
    	rememberLoginBox.setChecked(rememberPassword);
	}
    
    @Override
	protected void handleCoreServiceMsg(Context context,Bundle data) {
		if ( data.containsKey(LOGIN_RESULT))
		{
			if( data.getBoolean(LOGIN_RESULT)){
	        	Intent activityIntent = new Intent(context, DeviceListActivity.class);
	            startActivity(activityIntent);
	            finish();
			}
			else
			{
				//TODO some message about a failed login
			}
		}
	}

    @Override
    protected void onStop() {
    	super.onStop();
    	SharedPreferences settings = mHnadCoreService.getSettingsFile();
    	SharedPreferences.Editor editor = settings.edit();

    	String username = usernameText.getText().toString();
    	String password = passwordText.getText().toString();
    	boolean rememberPassword = rememberLoginBox.isChecked();
    	
    	editor.putString(SettingsKey.USERNAME, username);
    	editor.putBoolean(SettingsKey.REMEMBER_PASS, rememberPassword);
    	if( rememberPassword )
    		editor.putString(SettingsKey.PASSWORD, password);
    	else
    		editor.putString(SettingsKey.PASSWORD, "");
    	
    	editor.commit();
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
            	startActivity(new Intent(getApplicationContext(), ConfigActivity.class));
                break;
            case R.id.exit:
            	this.stopService(new Intent("cste.android.core.HNADCORESERVICE"));
            	finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	public Object onRetainNonConfigurationInstance() {
		return super.onRetainNonConfigurationInstance();
	}
}