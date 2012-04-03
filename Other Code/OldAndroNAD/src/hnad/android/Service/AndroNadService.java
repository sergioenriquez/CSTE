package hnad.android.Service;

import hnad.android.R;
import hnad.android.Activity.MainActivity;
import hnad.android.ICD.DeviceInfo;
import hnad.android.ICD.ICD;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * This is the base class for this app's connection service. The idea is that this base class will
 * contain all of the application logic, while your subclass will contain all of the communication logic
 * needed. There are also methods which you may override to provide any extra functionality needed.
 * 
 * When you subclass this, you will need to define at least a member class that extends Thread to run your
 * communication method. You will also need to have a way to stop your thread. (Usually you can just close the 
 * socket.)  You will also need to place your new service in the AndroidManifest.xml file.
 * 
 * @author Cory Sohrakoff
 *
 */
public abstract class AndroNadService extends Service {
	// For debugging
	private static final String TAG = AndroNadService.class.getName();
	private static final boolean D = true;
	
	/**
	 * Reference to the Handler that we can use to run things on the main thread, i.e.
	 * Toast messages.
	 */
	private Handler mRunnableHandler = new Handler();
	
	/**
	 * Post an arbitrary runnable to the main thread.
	 * 
	 * @param runnable
	 */
	public final void runOnMainThread(Runnable runnable) {
		mRunnableHandler.post(runnable);
	}
	
	/**
	 * Display a Toast message to the user, it will run on the main thread.
	 * 
	 * @param msg Message to be displayed.
	 */
	public final void toastMessage(final String msg) {
		runOnMainThread(new Runnable() {	
			@Override
			public void run() {
				Toast.makeText(AndroNadService.this, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	/**
	 * List of listeners that have registered with this Service.
	 */
	private final ArrayList<AndroNadService.EventListener> mListeners = new ArrayList<AndroNadService.EventListener>();
	
	/**
	 * Register the {@link AndroNadService.EventListener} to handle state/data changes within the service.
	 * 
	 * @param listener
	 */
	public final synchronized void addListener(AndroNadService.EventListener listener) {
		if (D) Log.d(TAG, "listener=" + listener);
		if (listener != null)
			mListeners.add(listener);
	}
	
	/**
	 * Unregister the {@link AndroNadService.EventListener} to handle state/data changes within the service.
	 * 
	 * @param listener
	 */
	public final synchronized void removeListener(AndroNadService.EventListener listener) {
		if (D) Log.d(TAG, "listener=" + listener);
		if (listener != null)
			mListeners.remove(listener);
	}
	
	/**
	 * Connected devices.
	 * 
	 * @return Copy of known device info.
	 */
	private HashMap<String, DeviceInfo> mDevices = new HashMap<String, DeviceInfo>();
	
	public final synchronized ArrayList<DeviceInfo> getAllDeviceInfo() {
		return new ArrayList<DeviceInfo>(mDevices.values());
	}
	
	/**
	 * Get the device info for a specified UID.
	 * 
	 * @param deviceUid
	 * @return		Latest device info, or null if none.
	 */
	public final synchronized DeviceInfo getDeviceInfo(String deviceUid) {
		return mDevices.get(deviceUid); // get returns null if deviceUid doesn't exist
	}
	
	public final ArrayList<String> getDeviceEventLog(String deviceUid) {
		DeviceInfo device = getDeviceInfo(deviceUid);
		if (device != null) {
			return device.getLog();
		}
		return null;
	}
	
	public final synchronized void clearDeviceEventLog(String deviceUid) {
		DeviceInfo device = getDeviceInfo(deviceUid);
		if (device != null) {
			device.clearLog();
			notifyDataUpdate();
		}
	}
	
	/**
	 * Put the device info in the list.
	 * 
	 * @param deviceInfo
	 */
	public final synchronized void addDeviceInfo(DeviceInfo deviceInfo) {
		if (mDevices != null && deviceInfo != null) {
			mDevices.put(deviceInfo.getUID(), deviceInfo);
		}
	}
	
	public final void sendUnrestrictedStatusCommand(String destinationUid) {
		icd.sendUnrestrictedStatusCommand(destinationUid);
	}
	
	public final void sendNopCommand(String destinationUid) {
		icd.sendNop(destinationUid);
	}

	public final void sendSetMasterAlarm(String destinationUid, boolean alarm) {
		icd.sendSetMasterAlarm(destinationUid, alarm);
	}
	
	public final void sendDisarm(String destinationUid) {
		icd.sendDisarm(destinationUid);
	}
	
	public final void sendSetTime(String destinationUid) {
		icd.sendSetTime(destinationUid);
	}
	
	/**
	 * Send trip info with either ARMT or CTI.
	 * @param destinationUid
	 * @param opcode
	 * @param mechanicalSealId
	 * @param conveyanceId
	 * @param manifest
	 */
	public void sendTripInformation(String destinationUid, byte opcode, String mechanicalSealId, 
			String conveyanceId, String manifest) {
		icd.sendTripInformation(destinationUid, opcode, mechanicalSealId, conveyanceId, manifest);
	}
	
	public void sendSetIntripState(String destinationUid, byte state) {
		icd.sendSetIntripState(destinationUid, state);
	} 
	
	public void sendLogCommand(String destinationUid, byte opcode) {
		icd.sendLogCommand(destinationUid, opcode);
	}
	
	public void setEncryption(boolean useEncryption) {
		preferences.edit().putBoolean(PREFERENCES_USE_ENCRYPTION, useEncryption).commit();
		icd.setEncryption(useEncryption);
	}
	
	public boolean useEncryption() {
		return icd.useEncryption();
	}
	
	public String getEncryptionKey() {
		return icd.getEncryptionKey();
	}
	
	public void setEncryptionKey(String newKey) {
		preferences.edit().putString(PREFERENCES_ENCRYPTION_KEY, newKey).commit();
		icd.setEncryptionKey(newKey);
		
	}
	
	private ICD icd;
	
    /**
     * The current state the service is in. It can be NONE (unconnected), CONNECTING, or CONNECTED,
     * depending on what stage of the connection the service is in.
     */
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE 			= 0; // we're doing nothing
	public static final int STATE_CONNECTING 	= 1; // now initiating an outgoing connection
	public static final int STATE_CONNECTED 	= 2; // now connected to a remote device
	public static final int STATE_DONE			= 3; // the service has finished running and will be stopping soon.
	
	/**
	 * App settings storage
	 */
	private SharedPreferences preferences;
	
	// name for preferences "file"
	private static final String SHARED_PREFERENCES_NAME = "Settings";
	
	// items stored in preferences
	private static final String PREFERENCES_ENCRYPTION_KEY = "ENCRYPTION_KEY";
	private static final String PREFERENCES_USE_ENCRYPTION = "USE_ENCRYPTION";
	
	/**
	 * Set the current state of the connection
	 * @param state  An integer constant defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		for (AndroNadService.EventListener listener : mListeners) {
			listener.onConnectionStateChanged(this, mState);
		}
	}

	/**
	 * Return the current connection state. 
	 */
	public final synchronized int getState() {
		 return mState;
	}
	
    /**
     * Method to be overridden to do whatever setup needed to begin connecting.
     */
    protected void connect() {}
    
    /**
     * Method to do whatever setup you need to do after the connection is started. If you override
     * make sure you call super.connect().
     */
    protected void connected() {
        if (D) Log.d(TAG, "connected");
        setState(STATE_CONNECTED);
        
		String title = getResources().getString(R.string.notification_title);
		Notification notification = new Notification(R.drawable.icon, title, 0);
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // flag stops a new activity from being created
														  // if  it is already running
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		String text = getResources().getString(R.string.notification_connected);
		notification.setLatestEventInfo(this, title, text, pendingIntent);
		startForeground(hashCode(), notification);
    }
    
    /**
     * Method to be overridden to do whatever teardown needed on a disconnect. If you override make 
     * sure you call super.connect(). You don't need to call this yourself.
     */
    private void disconnected() {
        if (D) Log.d(TAG, "disconnected");
        stopForeground(true);
    }
    
    /**
     * Stop all threads. You need to implement the log to stop your threads here. If you do not stop
     * them, they will continue to run after onDestroy(), making them orphans.
     */
    protected synchronized void stopThreads() {
        if (D) Log.d(TAG, "stop");
        setState(STATE_DONE);
        
        // stop timeout timers for devices
        for (DeviceInfo device : mDevices.values()) {
        	device.cancelRetryTimer();
        }
    }

    /**
	 * Write to the message out. You need to implement this when subclassing.
     */
    public abstract void sendMessage(String destinationUID, byte[] message);
    
    /**
	 * Process the message received from the connection per the ICD. Call this in your subclass
	 * once you have received a message. Don't overrride.
	 * 
	 * @param message
	 * @param length Message length in bytes
	 */
	protected final void parseMessage(byte[] message, int length) {
		icd.parseMessage(message, length);
		// TODO need better control of notify so that we dont have to call it always, good enough for now though
		notifyDataUpdate();
	}

	/**
     * Notify the UI that the data has changed.
     */
    private void notifyDataUpdate() {
    	synchronized (this) {
			if (mListeners.size() > 0) {
				for (AndroNadService.EventListener listener : mListeners)
					listener.onDataUpdated(this);
				// leave early since below is what we do if there are no listeners
				return;
			}
		}
    	if (D) Log.i(TAG, "No listeners attached.");
    	
    	// count how many devices have alarm
    	int alarmCount = 0;
    	for (DeviceInfo device : getAllDeviceInfo()) 
    		if (device.alarm())
    			alarmCount++;
    	// notification of alarms if there are any
    	if (alarmCount > 0) {
    		String title = getResources().getString(R.string.notification_alarm);
    		Notification notification = new Notification(R.drawable.alarm, title, 0);
    		
    		Intent intent = new Intent(this, MainActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // flag stops a new activity from being created
    														  // if  it is already running
    		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    		String text = alarmCount + " device" + (alarmCount == 1 ? "'s alarm is" : "s' alarms are") + " triggered.";
    		notification.setLatestEventInfo(this, title, text, pendingIntent);
    		
    		// add options to notification
    		// can also add sound or vibration if we want, but need to add the permissions to the manifest
    		notification.flags |= Notification.FLAG_AUTO_CANCEL;
    		
    		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		final int notificationId = 1;
    		notificationManager.notify(notificationId, notification);
    	}
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity. Do not override.
     */
    protected final void connectionFailed() {
        if (D) Log.e(TAG, "Connection Failed.");
        
        // Post a connection failed toast message.        
		toastMessage(getResources().getString(R.string.toast_connection_failed));
        
    	setState(STATE_DONE);
        stopSelf();
    }

    /**
     * Indicate that the connection was lost/terminated and notify the UI Activity. Do not override.
     */
    protected final void connectionTerminated() {
        if (D) Log.e(TAG, "Connection Lost.");
        
        // Post a connection lost toast message.
        toastMessage( getResources().getString(R.string.toast_connection_terminated));
        
        setState(STATE_DONE);
        disconnected();
        stopSelf();
    }
    
    /**
     * Class for UI to access service. 
     * 
     * @author Cory Sohrakoff
     *
     */
    public class LocalBinder extends Binder {
    	public AndroNadService getService() {
    		return AndroNadService.this;
    	}
    }
    
    /**
     * LocalBinder object.
     */
	private final IBinder mBinder = new LocalBinder();
	    
	/**
	 * Don't override this.
	 */
	public final IBinder onBind(Intent intent) {
		if(D) Log.i(TAG, "--- onBind() ---");
		return mBinder;
	}


	/**
	 * Don't override this. Implement serviceStarted() instead. This makes sure the
	 * serviceStarted code won't run if the service hasn't been started before.
	 */
	public final int onStartCommand(Intent intent, int flags, int startId) {
		if (getState() == STATE_NONE ) {
			serviceStarted(intent, flags, startId);
	        if (D) Log.d(TAG, "connecting");
	        setState(STATE_CONNECTING);
			connect();
		} else {
			if (D) Log.w(TAG, "Service already started.");
	        // Post an already connected toast message.
			toastMessage(getResources().getString(R.string.toast_already_connected));
		}
		// Want service to run until explicitly stopped so we return sticky.
		return START_STICKY;
	}
	
	/**
	 * Rather than overriding onStartCommand() place your code here to run when the service starts (i.e.
	 * get some extras from the Intent).
	 * 
	 * This will only be called if the service isn't already running.
	 * 
	 * @param intent
	 * @param flags
	 * @param startId
	 */
	protected void serviceStarted(Intent intent, int flags, int startId) {}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (D) Log.i(TAG, "onCreate()");
		setState(STATE_NONE);
		
		
		icd = new ICD(this, null); // TODO deviceUID not null, unless we want to use default
		
		// get settings
		preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		String key = preferences.getString(PREFERENCES_ENCRYPTION_KEY, icd.getEncryptionKey());
		icd.setEncryptionKey(key);
		boolean useEncryption = preferences.getBoolean(PREFERENCES_USE_ENCRYPTION, icd.useEncryption());
		icd.setEncryption(useEncryption);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D) Log.i(TAG, "onDestroy()");
		stopThreads();			
	}

	/**
	 * Provides a means for Activities to receive data from the {@link AndroNadService}. They can
	 * register their version of this Listener with the Service in order to receive notifications for
	 * certain events defined here.
	 * 
	 * This callbacks will occur on whatever thread they originate from (i.e. where notify...() was called from). 
	 * This means if you modify the UI in a callback you should wrap that code in a Runnable and
	 * run it on the main thread. 
	 * 
	 * You set the specific subclass you will be using in AndroNadActivity. It has constant
	 * called ANDRO_NAD_SERVICE_CLASS.
	 * 
	 * @author Cory Sohrakoff
	 *
	 */
	public static abstract class EventListener {			
		/**
		 * This method should be implemented to allow the UI to reflect the changes in the connection 
		 * state of the AndroNadService.
		 * 
		 * @param service 			AndroNadService that is making this callback.
		 * @param connectionState	Integer state defined in {@link AndroNadService}.
		 */
		public abstract void onConnectionStateChanged(final AndroNadService service, final int connectionState);
		
		/**
		 * Implement here what the application needs to do when the data in the AndroNadService has been 
		 * updated. NOTE: This gives you the new data. If you want all of the data, you will need
		 * to fetch the data yourself.
		 * 
		 * @param service 		AndroNadService that is making this callback.
		 */
		public abstract void onDataUpdated(final AndroNadService service);
	}
}
