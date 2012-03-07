package cste.android.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.os.Handler;
import cste.components.ComModule;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.NadaTimeDelay;
import cste.messages.NADA;
import cste.misc.XbeeAPI;
import static cste.icd.Utility.*;

//TODO clear channel assesment?

public class NADABroadcaster implements Runnable{
	protected boolean enabled = false;
	protected int msgSendCnt = 0;
	protected NadaTimeDelay delayCode = NadaTimeDelay.D100;
	
	protected Handler mHandler;
	protected UsbCommManager mUsbCommHandler;
	protected HNADService mParent;
	
	//burst tx 1 sec then 1 sec quiet
	protected int BurstCount = 13;
	protected int LongDelay = 1000;
	
	
	protected DeviceType dcpType;
	protected DeviceUID dcpUID;
	protected DeviceType lvl2Type;
	protected DeviceUID lvl2UID;
	protected List<DeviceUID> mMsgWaitingList;
	
	protected static final int MAX_MSG_WAITING_CNT = 5;
	
	public NADABroadcaster(HNADService parent, Handler handler, UsbCommManager usbHandler)
	{
		mHandler = handler;
		mUsbCommHandler = usbHandler;
		mParent = parent;
		mMsgWaitingList = new ArrayList<DeviceUID>();
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
		
		mMsgWaitingList.clear();
		Enumeration<ComModule> devices = mParent.getDeviceList().elements();
		int msgWaitCnt = 0;
		while(devices.hasMoreElements())
		{
			ComModule dev = devices.nextElement();
			if( dev.pendingTxMsgCnt > 0 && msgWaitCnt < MAX_MSG_WAITING_CNT){
				msgWaitCnt++;
				mMsgWaitingList.add(dev.UID());
			}
		}

		NADA nadaMsg = new NADA(DeviceType.FNAD_I,
								delayCode,
								dcpType,
								dcpUID,
								lvl2Type,
								lvl2UID,
								mMsgWaitingList);
		
		XbeeAPI.transmitPkt(XbeeAPI.BROADCAST_ADDRESS, nadaMsg.getBytes());

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
