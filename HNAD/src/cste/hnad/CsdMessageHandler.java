package cste.hnad;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cste.android.core.HNADService;
import cste.misc.XbeeAPI;

public class CsdMessageHandler extends Handler {
	private static final String TAG = "CSD Msg Handler";
	
	public static final int DEVICE_CONNECTED 	= 1;
	public static final int DEVICE_DISCONNECTED = 2;
	public static final int MSG_RECEIVED 		= 3;

	private HNADService mHnadCore;
	
	
	public CsdMessageHandler(HNADService hnad){
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
			//Log.i(TAG,"Pkt from ");
			break;
		default:
			Log.w(TAG,"Unknown message type");
			break;

		}
	}

}
