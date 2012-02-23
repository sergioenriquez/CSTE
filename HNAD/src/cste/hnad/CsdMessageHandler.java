package cste.hnad;

import cste.messages.IcdMsg;
import cste.misc.ZigbeeAPI;
import cste.misc.ZigbeePkt;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CsdMessageHandler extends Handler {
	private static final String TAG = "CSD Msg Handler";
	
	public static final int DEVICE_CONNECTED = 1;
	public static final int DEVICE_DISCONNECTED = 2;
	public static final int PACKET_RECEIVED = 3;
	
	private HnadCoreInterface mHnadCore;
	
	
	public CsdMessageHandler(HnadCoreInterface hnad){
		this.mHnadCore = hnad;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case DEVICE_CONNECTED:
			mHnadCore.onUsbStateChanged(true);
			break;
		case DEVICE_DISCONNECTED:
			mHnadCore.onUsbStateChanged(false);
			break;
		case PACKET_RECEIVED:
			byte[] rawData = msg.getData().getByteArray("content");
			ZigbeePkt pkt = ZigbeeAPI.parsePkt(rawData);
			mHnadCore.onPacketReceived(pkt);
			break;
		default:

			break;

		}
	}

}
