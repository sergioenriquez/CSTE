package cste.android.core;

import java.util.ArrayList;

import android.os.Handler;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.icd_messages.NADA;
import cste.icd.types.DeviceType;
import cste.icd.types.DeviceUID;
import cste.icd.types.NadaTimeDelay;

public class NADABroadcaster implements Runnable{
	static final String TAG = "NADA broadcaster";

	protected boolean enabled = false;
	protected int msgSendCnt = 0;
	protected NadaTimeDelay delayCode = NadaTimeDelay.D100;
	protected Handler mHandler;
	protected UsbCommManager mUsbCommHandler;
	protected HNADService mParent;
	
	protected int BurstCount = 13;
	protected int LongDelay = 1000;
	protected DeviceType dcpType;
	protected DeviceUID dcpUID;
	protected DeviceType lvl2Type;
	protected DeviceUID lvl2UID;
	protected ArrayList<DeviceUID> mWaitingList;
	protected boolean discoveryMode = false;
	protected final DeviceUID DiscoveryUID = new DeviceUID("FFFFFFFFFFFFFFFF");
	
	public void setDeviceDiscoveryMode(boolean state){
		discoveryMode = state;
		updateWaitingList();
	}
	
	protected static final int MAX_MSG_WAITING_CNT = 5;
	
	public NADABroadcaster(HNADService parent, Handler handler, UsbCommManager usbHandler){
		mHandler = handler;
		mUsbCommHandler = usbHandler;
		mParent = parent;
		discoveryMode = false;
		mWaitingList = new ArrayList<DeviceUID>(5);
	}
	
	public void config(int burstIndex,  DeviceType lvl2Type, DeviceUID lvl2UID, DeviceType dcpType, DeviceUID dcpUID){
		this.lvl2Type = lvl2Type;
		this.lvl2UID = lvl2UID;
		
		this.dcpType = dcpType;
		this.dcpUID = dcpUID;
		
		delayCode = NadaTimeDelay.fromIndex(burstIndex);
		BurstCount = 1000 / delayCode.getMsDelay();
	}
	
	public void updateWaitingList(){
		mWaitingList.clear();
		
		for(IcdMsg msg: mParent.getTxList() ){
			if(msg != null && !mWaitingList.contains(msg.destUID))
				mWaitingList.add(msg.destUID);
		}
		
		if( discoveryMode )
			mWaitingList.add(DiscoveryUID);
	}

	@Override
	public void run(){
		enabled = true;

		NADA nadaMsg = new NADA(DeviceType.FNAD_I,
								delayCode,
								dcpType,
								dcpUID,
								lvl2Type,
								lvl2UID,
								mWaitingList);
		
		XbeeAPI.transmitPkt(XbeeAPI.BROADCAST_ADDRESS, nadaMsg.getBytes());

		if (enabled){
			msgSendCnt++;
			if( msgSendCnt < BurstCount)
				mHandler.postDelayed(this, delayCode.getMsDelay());
			else{
				mHandler.postDelayed(this, LongDelay);
				msgSendCnt = 0;
			}
		}
	}//end run
}
