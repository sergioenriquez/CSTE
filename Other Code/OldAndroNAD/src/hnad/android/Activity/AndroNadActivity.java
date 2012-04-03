package hnad.android.Activity;

import hnad.android.R;
import hnad.android.Service.AndroNadService;
import hnad.android.Service.UdpAndroNadService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * This is the base class for all of the other Activities that bind to the {@link AndroNadService}. This
 * class does all of the basic setup for binding and leaves stub methods to implement any extra set-up.
 * It creates the custom title bar and Listener for the AndroNadService.
 * 
 * We don't want to instantiate a version of this class since it has no UI
 * defined. That is left to the subclasses.
 * 
 * @author Cory Sohrakoff
 *
 */
public abstract class AndroNadActivity extends Activity {
	// For debugging
    private static final String TAG = AndroNadActivity.class.getName();
    private static final boolean D = true;

    /**************************************************************************
     * Which subclass of {@link AndroNadService} we will be using for this app.
     */
    @SuppressWarnings("rawtypes")
	private Class ANDRO_NAD_SERVICE_CLASS = UdpAndroNadService.class;
    
    
    // Layout Views
    private TextView mTitle;
    private ProgressBar mProgressBar; // indeterminate progress bar
    
    /**
     * Reference to the AndroNadService.
     */
    private AndroNadService mAndroNadService;
    
    /**
     * Get the AndroNadService. Could be null.
     * @return AndroNadService instance, or null.
     */
    protected final AndroNadService getAndroNadService() {
    	return mAndroNadService;
    }
    
    /**
     * This handles the connecting and disconnecting of the service from the UI.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
		/**
		 * When the service is disconnected, the reference we keep is set to null.
		 * We will also unregister our listener.
		 */
		@Override
		public void onServiceDisconnected(ComponentName className) {
			if (D) Log.i(TAG, "Service disconnected.");
			
			mAndroNadService = null;
			
			// set disconnected in title
			mTitle.setText(R.string.title_not_connected);
			
			AndroNadActivity.this.onServiceDisconnected();
			
			// *** weird hack ***
	    	// reset UI/service binding
			// for some reason if this isn't done after the service is disconnected
			// we won't get a notification when the service connects after the first time
			// even though the documentation for ServiceConnection states that we should
	        boolean bindSuccessful = bindService(new Intent(AndroNadActivity.this, ANDRO_NAD_SERVICE_CLASS), mConnection, 0);
	        if (D) Log.d(TAG, "bindSuccessful=" + bindSuccessful);
		}
		
		/**
		 * When the service is connected, the reference to it is set to the service
		 * instance. We will also register our listener with the service.
		 */
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
        	if (D) Log.i(TAG, "Service connected.");
			
        	mAndroNadService = ((AndroNadService.LocalBinder)service).getService();
			
			// register UI's listener for "callbacks"
			mAndroNadService.addListener(mListener);
        	
			// update app to service's current state
			setTitleBarForServiceState(mAndroNadService.getState());
			AndroNadActivity.this.onServiceConnected(mAndroNadService);
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.empty); // set blank layout
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set up the custom title bar
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mTitle.setText(R.string.title_not_connected);
        mProgressBar = (ProgressBar) findViewById(R.id.title_progress_bar);
        
    	// set up UI/service binding
        boolean bindSuccessful = bindService(new Intent(AndroNadActivity.this, ANDRO_NAD_SERVICE_CLASS), mConnection, 0);
        if (D) Log.d(TAG, "bindSuccessful=" + bindSuccessful);
    }
    
    /**
     * Use this to set the left-hand title in the custom title bar. (Unless you want to keep the default 
     * title).
     * 
     * @param stringResId Resource id for the string to set in the title bar.
     */
    protected final void setLeftTitle(int stringResId) {
    	TextView leftTitle = (TextView) findViewById(R.id.title_left_text);
    	leftTitle.setText(stringResId);
    }
    
    /**
     * Starts the AndroNadService subclass. Call this in subclass to start the service.
     */
    protected final void startAndroNadService() {
    	Intent serviceIntent = new Intent(this, ANDRO_NAD_SERVICE_CLASS);
    	startService(serviceIntent);
    }
    
    /**
     * Stops the AndroNadService subclass. Call this in subclass to start the service.
     */
    protected final void stopAndroNadService() {
    	Intent serviceIntent = new Intent(this, ANDRO_NAD_SERVICE_CLASS);
    	stopService(serviceIntent);
    }
    
    @Override
    public void onDestroy() {
        // Stop the Bluetooth services
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        
        // unregister the listener
        if (mAndroNadService != null)
        	mAndroNadService.removeListener(mListener);
        
        // remove UI/service binding
        unbindService(mConnection);
        
        super.onDestroy();
    }

	/**
	 * Listener to handle service notifications when we are observing.
	 */
	private final AndroNadService.EventListener mListener = new AndroNadService.EventListener() {
		@Override
		public void onConnectionStateChanged(final AndroNadService service, final int connectionState) {
			AndroNadActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setTitleBarForServiceState(connectionState);
					AndroNadActivity.this.onConnectionStateChanged(service, connectionState);
				}
			});
		}
		
		@Override
		public void onDataUpdated(final AndroNadService service) {
			AndroNadActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AndroNadActivity.this.onDataUpdated(service);
				}
			});
		}
	};
	
	/**
	 * Handles changing the app title bar based on the service's current state.
	 * @param serviceState
	 */
	private void setTitleBarForServiceState(int connectionState) {
		switch (connectionState) {
		case AndroNadService.STATE_DONE:
			// same as STATE_NONE so fall through
		case AndroNadService.STATE_NONE:
			mTitle.setText(R.string.title_not_connected);
			mProgressBar.setVisibility(View.GONE);
			break;
		case AndroNadService.STATE_CONNECTING:
			mTitle.setText(R.string.title_connecting);
			mProgressBar.setVisibility(View.VISIBLE);
			break;
		case AndroNadService.STATE_CONNECTED:
			mTitle.setText(R.string.title_connected);
			mProgressBar.setVisibility(View.GONE);
			break;
		}
	}
	
	/* --- Methods to override for extra functionality in subclasses. --- */
	
	/**
	 * This method does nothing by default. Override to perform actions based on the state of the
	 * AndroNadService. This runs on the main thread, so it is safe to modify UI from here.
	 * 
	 * @param service
	 * @param connectionState
	 */
	protected void onConnectionStateChanged(AndroNadService service, int connectionState) {}
	
	/**
	 * This method does nothing by default. Override to perform actions based on the AndroNadService
	 * receiving new data.
	 * 
	 * @param service		AndroNadService that is making this callback.
	 */
	protected void onDataUpdated(AndroNadService service) {}
	
	/**
	 * This method does nothing by default. It is called when the AndroNadService is
	 * connected to the Activity. This runs on the main thread, so it is safe to modify UI from here.
	 * 
	 * @param service
	 */
	protected void onServiceConnected(AndroNadService service) {}
	
	/**
	 * This method does nothing by default. It is called when the AndroNadService is
	 * disconnected from the Activity.
	 */
	protected void onServiceDisconnected() {}
}

