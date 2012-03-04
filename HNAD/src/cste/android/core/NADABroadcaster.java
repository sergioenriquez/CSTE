package cste.android.core;

import android.os.Handler;
import cste.icd.NadaTimeDelay;
import cste.messages.NADA;
import cste.misc.ZigbeeAPI;
import static cste.icd.Utility.*;

//TODO clear channel assesment?

public class NADABroadcaster implements Runnable{
	private boolean enabled = false;
	private int msgSendCnt = 0;
	private NadaTimeDelay delayCode = NadaTimeDelay.D100;
	
	private Handler mHandler;
	private UsbCommManager mUsbCommHandler;
	private HNADService mParent;
	
	//burst tx rate of 80ms for 1 sec, then 1.0 sec quiet
	private final int BURST_CNT = 13;
	private final int LONG_DELAY = 960;
	private final byte[] BROADCAST_ADDRESS	= strToHex("000000000000FFFF");
	
	public NADABroadcaster(HNADService parent, Handler handler, UsbCommManager usbHandler)
	{
		mHandler = handler;
		mUsbCommHandler = usbHandler;
		mParent = parent;
	}

	@Override
	public void run() {
		enabled = true;

		NADA nadaMsg = new NADA(mParent.thisDevType,
								delayCode,
								mParent.dcpDevType,
								mParent.dcpUID,
								mParent.lvl2DevType,
								mParent.lvl2UID,
								mParent.mMsgWaitingList);
		
		byte [] zigbeePkt = ZigbeeAPI.buildPkt(BROADCAST_ADDRESS,(byte)0x00,nadaMsg.getBytes());
		
		if( !mUsbCommHandler.transmit(zigbeePkt) )
			enabled = false;

		if (enabled)
		{
			msgSendCnt++;
			if( msgSendCnt < BURST_CNT)
				mHandler.postDelayed(this, delayCode.getMsDelay());
			else
			{
				mHandler.postDelayed(this, LONG_DELAY);
				msgSendCnt = 0;
			}
		}
	}//end run
	
	
}
