package cste.android.network;

import java.util.concurrent.ArrayBlockingQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cste.android.core.IcdMessageHandler;
import cste.icd.icd_messages.IcdMsg;
import cste.icd.net_messages.HeartBeatResponse;
import cste.icd.net_messages.NetPkt;
import cste.icd.types.MaintenaceCode;

public class NetworkManager{
	static final String TAG = "Network handler";
	
	protected String serverAddress;
	protected int serverPort;
	protected Handler mHandler;
	protected ArrayBlockingQueue<NetPkt> pendingTxList;
	protected HnadClientThread thread;

	public void config(String server, int port){
		serverAddress = server;
		serverPort = port;
	}
	
	public void stop(){
		thread.isEnabled = false;
	}
	
	public void onPktReceived(NetPkt pkt){
		Message m = Message.obtain(mHandler);
		
		switch(pkt.preamble.functionCode){
		case HEARTBEAT_REPLY:
			m.what = IcdMessageHandler.DCP_LOGIN_RESULT;
			HeartBeatResponse heartBeat =  (HeartBeatResponse)pkt.payload;
			Bundle data = new Bundle();
			data.putSerializable("maintenaceCode", heartBeat.maintenaceCode);
			data.putSerializable("timestamp", heartBeat.timestamp);
			m.setData(data);
			m.sendToTarget();
			break;
		case DCP_CONFIG_CHANGE:
			m.what = IcdMessageHandler.DCP_CONFIG;
			m.sendToTarget();
			break;
		default:
			Log.w(TAG, "Received an unhandled funtion code");
		}
	}
	
	public NetworkManager(Handler handler){
		serverAddress = "";
		serverPort = 0;
		pendingTxList = new ArrayBlockingQueue<NetPkt>(5); 
		mHandler = handler;
		thread = new HnadClientThread(this);
		new Thread(thread).start();
	}
	
	public void sendIcdMsg(IcdMsg msg){
		NetPkt pkt = NetPkt.buildIcdtMsg(msg);
		pendingTxList.add(pkt);
	}
	
	public void sendHeartBeat(MaintenaceCode code){
		NetPkt pkt = NetPkt.buildHeartbeatMsg(code);
		pendingTxList.add(pkt);
	}
	
	public void loginToDCP(String username,String password){
		NetPkt pkt = NetPkt.buildLoginRequestMsg(username, password);
		pendingTxList.add(pkt);
	}
	
	public int getPendingPktCount(){
		return pendingTxList.size();
	}
	
	public NetPkt getNextPkt(){
		try {
			return pendingTxList.take();
		} catch (InterruptedException e) {
			Log.e(TAG, "Thread Interrupted");
			return null;
		}
	}
}
