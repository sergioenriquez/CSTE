package cste.hnad;

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
			//SwitchMsg o = (SwitchMsg) msg.obj;
			//handleSwitchMessage(o);
			int content = msg.arg1;
			
			mHnadCore.packetReceived(content);
			// signal something
			break;
		default:

			break;

		}
	}

}
