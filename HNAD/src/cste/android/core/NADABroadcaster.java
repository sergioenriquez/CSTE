package cste.android.core;

import android.os.Handler;
import cste.messages.NADA;
import cste.misc.ZigbeeAPI;

//TODO clear channel assesment?

public class NADABroadcaster implements Runnable{
	private boolean enabled = false;
	private int msgSendCnt = 0;
	
	private Handler mHandler;
	private UsbCommHandler mUsbCommHandler;
	private HnadCoreService mParent;
	
	//burst tx rate of 20ms for 1 sec, then 0.5 sec quiet
	private final int BURST_CNT = 50;
	private final int SHORT_DELAY = 20;
	private final int LONG_DELAY = 500;
	private final byte[] BROADCAST_ADDRESS	= hexStringToByteArray("000000000000FFFF");
	
	public NADABroadcaster(HnadCoreService parent, Handler handler, UsbCommHandler usbHandler)
	{
		mHandler = handler;
		mUsbCommHandler = usbHandler;
		mParent = parent;
	}

	@Override
	public void run() {
		enabled = true;

		NADA nadaMsg = new NADA(mParent.thisDevType,
								0x01,
								mParent.dcpDevType,
								mParent.dcpUID,
								mParent.lvl2DevType,
								mParent.lvl2UID,
								mParent.mMsgWaitingList);
		
		byte [] zigbeePkt = ZigbeeAPI.buildPkt(BROADCAST_ADDRESS,nadaMsg.getBytes());
		
		if( !mUsbCommHandler.transmit(zigbeePkt) )
			enabled = false;

		if (enabled)
		{
			msgSendCnt++;
			if( msgSendCnt < BURST_CNT)
				mHandler.postDelayed(this, SHORT_DELAY);
			else
			{
				mHandler.postDelayed(this, LONG_DELAY);
				msgSendCnt = 0;
			}
		}
	}//end run
	
	public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
