package cste.android.core;

import android.os.Handler;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
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
	
	//burst tx 1 sec then 1 sec quiet
	private int BurstCount = 13;
	private int LongDelay = 1000;
	private final byte[] BROADCAST_ADDRESS	= strToHex("000000000000FFFF");
	
	DeviceType dcpType;
	DeviceUID dcpUID;
	DeviceType lvl2Type;
	DeviceUID lvl2UID;
	
	public NADABroadcaster(HNADService parent, Handler handler, UsbCommManager usbHandler)
	{
		mHandler = handler;
		mUsbCommHandler = usbHandler;
		mParent = parent;
	}
	
	public void config(int burstIndex,  DeviceType lvl2Type, DeviceUID lvl2UID, DeviceType dcpType, DeviceUID dcpUID){
		this.lvl2Type = lvl2Type;
		this.lvl2UID = lvl2UID;
		
		this.dcpType = dcpType;
		this.dcpUID = dcpUID;
		
		delayCode = NadaTimeDelay.fromIndex(burstIndex);
		BurstCount = 1000 / delayCode.getMsDelay();
	}

	@Override
	public void run() {
		enabled = true;

		NADA nadaMsg = new NADA(DeviceType.FNAD_I,
								delayCode,
								dcpType,
								dcpUID,
								lvl2Type,
								lvl2UID,
								mParent.mMsgWaitingList);
		
		byte [] zigbeePkt = ZigbeeAPI.buildPkt(BROADCAST_ADDRESS,(byte)0x00,nadaMsg.getBytes());
		
		if( !mUsbCommHandler.transmit(zigbeePkt) )
			enabled = false;

		if (enabled)
		{
			msgSendCnt++;
			if( msgSendCnt < BurstCount)
				mHandler.postDelayed(this, delayCode.getMsDelay());
			else
			{
				mHandler.postDelayed(this, LongDelay);
				msgSendCnt = 0;
			}
		}
	}//end run
	
	
}
