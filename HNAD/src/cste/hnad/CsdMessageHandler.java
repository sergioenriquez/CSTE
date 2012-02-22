package cste.hnad;

import cste.messages.IcdMsg;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class CsdMessageHandler extends Handler {
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
			byte[] packet = msg.getData().getByteArray("content");
			IcdMsg icdMsg = new IcdMsg(packet);

			mHnadCore.onPacketReceived(icdMsg);
			// signal something
			break;
		default:

			break;

		}
	}

}
