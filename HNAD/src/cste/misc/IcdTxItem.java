package cste.misc;

import cste.android.core.HNADService;
import cste.icd.DeviceUID;
import cste.messages.IcdMsg;
import android.os.Handler;

public class IcdTxItem implements Runnable {
	HNADService service;
	DeviceUID destUID;

	public IcdMsg msgSent;
	
	public int retryAttempts;
	protected Handler mHandler;

	public IcdTxItem(HNADService service, DeviceUID destUID, IcdMsg msgSent){
		this.service = service;
		this.destUID = destUID;
		this.msgSent = msgSent;
		
		this.mHandler = new Handler();
		this.retryAttempts = 0;
		
		restartTimer();
	}
	
	public void clearTimer(){
		mHandler.removeCallbacks(this);
	}
	
	public void restartTimer(){
		mHandler.postDelayed(this, 1000);
	}

	@Override
	public void run() {
		//service.onRadioTransmitResult(false, destUID, (short)(msgSent.headerData.msgAsc & 0xFF));
	}
}
