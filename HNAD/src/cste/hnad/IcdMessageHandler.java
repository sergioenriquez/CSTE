package cste.hnad;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cste.android.core.HNADService;
import cste.misc.XbeeAPI;

public class IcdMessageHandler extends Handler {
	private static final String TAG = "CSD Msg Handler";
	
	public static final int DEVICE_CONNECTED 	= 1;
	public static final int DEVICE_DISCONNECTED = 2;
	public static final int MSG_RECEIVED 		= 3;
	
	public static final int DCP_HEARTBEAT 		= 4;
	public static final int DCP_ICD_MSG_RECEIVED= 5;
	public static final int DCP_CONFIG	 		= 6;
	public static final int DCP_LOGIN_RESULT	= 7;
	public static final int DCP_KEYS_RECEIVED	= 8;

	private HNADService mHnadCore;
	
	
	public IcdMessageHandler(HNADService hnad){
		this.mHnadCore = hnad;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case DEVICE_CONNECTED:
			mHnadCore.onUsbStateChanged(true);
			Log.i(TAG,"USB host connected");
			break;
		case DEVICE_DISCONNECTED:
			mHnadCore.onUsbStateChanged(false);
			Log.i(TAG,"USB host disconnected");
			break;
		case MSG_RECEIVED:
			byte[] rawData = msg.getData().getByteArray("content");
			int size = msg.getData().getInt("size");
			XbeeAPI.parseFrame(rawData,size);
			break;
			
		case DCP_HEARTBEAT:
			break;
		case DCP_ICD_MSG_RECEIVED:
			break;
		case DCP_CONFIG:
			break;
		case DCP_LOGIN_RESULT:
			boolean result = msg.getData().getBoolean("loginResult");
			mHnadCore.onLoginResult(result);
			break;
		case DCP_KEYS_RECEIVED:
			break;
			
		default:
			Log.w(TAG,"Unknown message type");
			break;

		}
	}

}
