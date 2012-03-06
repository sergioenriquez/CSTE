package cste.misc;

import android.os.Handler;

public class RetryTxItem implements Runnable {
	public byte ackNo;
	public int timeout;
	public int retryAttempts;
	protected Handler mHandler;
	public byte[] payload;

	public RetryTxItem(byte ackNo, byte[] payload){
		this.ackNo = ackNo;
		this.mHandler = new Handler();
		this.retryAttempts = 0;
		this.payload = payload;
		restartTimer();
	}
	
	public void clearTimer(){
		mHandler.removeCallbacks(this);
	}
	
	public void restartTimer(){
		mHandler.postDelayed(this, XbeeAPI.TIMEOUT_PERIOD);
	}

	@Override
	public void run() {
		XbeeAPI.onTransmitResult(false,ackNo);
	}
}
