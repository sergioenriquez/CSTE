package cste.android.activities;

import static cste.android.core.HnadCoreService.Events.LOGIN_RESULT;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import cste.android.R;

/***
 * Lets user enter their login credentials to authenticated with the DCP server. If successful it will then launch the device details activity.
 * @author User
 *
 */
public class LoginActivity extends HnadBaseActivity {
	static final String TAG = "HNAD Login";
	private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.login);
        loginButton = (Button)findViewById(R.id.authLoginBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//TODO show progress bar
            	mHnadCoreService.login("username", "password");
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mHnadCoreService.uploadData(); //TODO just a test
       
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