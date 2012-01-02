package cste.hnad;

import cste.messages.IcdMsg;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class CsdMessageHandler extends Handler {
	public static final int PACKET_RECEIVED = 1;
	private HnadCoreInterface mHnadCore;
	
	
	public CsdMessageHandler(HnadCoreInterface hnad){
		this.mHnadCore = hnad;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PACKET_RECEIVED:
			byte[] packet = msg.getData().getByteArray("content");
			IcdMsg icdMsg = new IcdMsg(packet);

			mHnadCore.packetReceived(icdMsg);
			// signal something
			break;
		default:

			break;

		}
	}

}
