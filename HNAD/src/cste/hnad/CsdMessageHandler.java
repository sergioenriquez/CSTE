package cste.hnad;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cste.misc.ZigbeeAPI;
import cste.misc.ZigbeeFrame;

public class CsdMessageHandler extends Handler {
	private static final String TAG = "CSD Msg Handler";
	
	public static final int DEVICE_CONNECTED 	= 1;
	public static final int DEVICE_DISCONNECTED = 2;
	public static final int MSG_RECEIVED 		= 3;

	private HnadCoreInterface mHnadCore;
	
	
	public CsdMessageHandler(HnadCoreInterface hnad){
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
			ZigbeeFrame pkt = ZigbeeAPI.parsePkt(rawData);
			mHnadCore.onPacketReceived(pkt);
			Log.i(TAG,"Pkt from ");
			break;
		default:
			Log.w(TAG,"Unknown message type");
			break;

		}
	}

}
