package cste.misc;

import static cste.icd.Utility.strToHex;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;

import cste.android.core.HNADService;
import cste.components.ComModule;
import cste.hnad.HNADServiceInterface;
import cste.hnad.RadioCommInterface;
import android.util.Log;
import android.widget.Toast;

public class XbeeAPI {
	private static final String TAG = "Zigbee API";
	
	private static final int ADDR_SIZE = 8;
	private static final int OVERHEAD = 7 + ADDR_SIZE;
	private static final byte DELIMETER = 0x7E;
	private static final byte CMD_64BIT = 0x00;
	
	public static final byte RX_64BIT = (byte) 0x80;
	public static final byte RX_16BIT = (byte) 0x81;
	public static final byte TX_STATUS = (byte) 0x89;

	private static final byte NO_ACK = 0x01;
	private static final byte ACK_REQ = 0x00;
	
	public static final byte[] BROADCAST_ADDRESS = strToHex("000000000000FFFF");
	public static final byte[] BAD_ADDRESS = strToHex("0000000000000000");
	
	private static RadioCommInterface comInterface;
	public static HNADServiceInterface mHnadService; // TODO replace with interface

	protected static byte nextFrameAck = 0;
	protected static Hashtable<Byte,RetryTxItem> txTable = new Hashtable<Byte, RetryTxItem>();
	
	public static final int MAX_RETRY_ATTEMPTS = 3;
	public static final int TIMEOUT_PERIOD = 1000;

	//BCAST
	
	/***
	 * 
	 */
	public static void setRadioInterface(RadioCommInterface radioInterface){
		comInterface = radioInterface;
	}
	
	public static void setHnadService(HNADServiceInterface mHnadService){
		XbeeAPI.mHnadService = mHnadService;
	}
	
	public static synchronized void onTransmitResult(boolean success, byte frameAck){
		RetryTxItem item = txTable.get(frameAck);
		if( item == null){
			Log.w(TAG,"Retransmit table item not found");
			return;
		}
		item.clearTimer();
		
		if( success )
		{
			txTable.remove(frameAck);
			mHnadService.onRadioTransmitResult(true,item.destination);
		}
		else{
			if(item.retryAttempts < MAX_RETRY_ATTEMPTS){
				Log.i(TAG,"No ACK received, retrying");
				comInterface.transmit(item.payload);
				item.retryAttempts++;
				item.restartTimer();
			}else{
				txTable.remove(frameAck);
				mHnadService.onRadioTransmitResult(false,item.destination);
				Toast.makeText(mHnadService.getContext(), "No reply received", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public static synchronized void transmitPkt(byte []dest, byte []payload){
		if( dest == null || payload == null){
			Log.e(TAG,"Tried to transmit a null packet");
			mHnadService.onRadioTransmitResult(false,dest);
			return;
		}
		
		boolean needAck = dest.equals(BROADCAST_ADDRESS) ? false : true;
		
		byte[] frame = buildFrame(dest,payload,needAck);
		boolean txResult = comInterface.transmit(frame);
		
		if( needAck ){
			if ( txResult ){
				txTable.put(nextFrameAck,  new RetryTxItem(nextFrameAck, frame, dest) );
				nextFrameAck++;
			}else{
				Toast.makeText(mHnadService.getContext(), "USB interface not availible", Toast.LENGTH_SHORT).show();
				mHnadService.onRadioTransmitResult(false,dest);
			}
		}
	}

	/***
	 * Based on API level 2, supports 64 bit addresses with no ACK
	 * @param dest
	 * @param msg
	 * @return
	 */
	protected static byte[] buildFrame(byte []dest, byte []msg, boolean needAck){
		ByteBuffer tmp = ByteBuffer.allocate(msg.length + OVERHEAD);
		if( dest.length != ADDR_SIZE)
			return tmp.array(); // only 64bit address supported 

		tmp.put(CMD_64BIT);
		tmp.put( needAck ? nextFrameAck : (byte)0);
		tmp.put(dest);
		tmp.put( needAck ? ACK_REQ : NO_ACK);
		tmp.put(msg);
		
		byte sum = 0x00;
		for( int i=0 ; i < OVERHEAD+msg.length-1 ; i++ )
			sum += tmp.get(i);
		tmp.put((byte) (0xFF - sum));
		
		//escape special chars
		ByteBuffer escapedSeq = ByteBuffer.allocate(tmp.capacity()*2);
		int newSize=0;
		escapedSeq.put(DELIMETER);
		escapedSeq.put((byte) 0x00);
		escapedSeq.put((byte)(msg.length + ADDR_SIZE + 3));
		for(int s=0; s<tmp.capacity() ; s++,newSize++)
		{
			byte c = tmp.get(s);
			if( c == 0x7E || 
				c == 0x7D ||
				c == 0x11 ||
				c == 0x13)
			{
				escapedSeq.put((byte)0x7D);
				escapedSeq.put((byte) (c ^ 0x20));
				newSize++;
			}
			else
				escapedSeq.put(c);
		}
		byte []zigbeePkt = new byte[newSize];
		escapedSeq.rewind();
		escapedSeq.get(zigbeePkt);
		
		return zigbeePkt;
	}
	
	private static boolean checksumOK(ByteBuffer buffer){
		buffer.get();//delimeter
		short size = buffer.getShort();//size
		if( size > 64){
			Log.w(TAG,"test 1");
		}
		
		int i;
		byte sum = 0;
		for(i=3;i<size+3;i++){
			if( i >= buffer.capacity())
				return false;
			sum += buffer.get(i);
		}

		byte calcCheckSum = (byte) (0xFF  - sum);
		byte checksum = buffer.get(i);
		buffer.rewind();
		if ( calcCheckSum == checksum)
			return true;
		else
			return false;
	}

	//TODO handle other msg types
	/***
	 * Unwraps the data delivered by the Zigbee transceiver
	 * @param msg
	 * @return
	 */
	public static void parseFrame(byte[] msg,int sizeIn)
	{
		ByteBuffer temp = ByteBuffer.allocate(sizeIn);
		temp.put(msg, 0, sizeIn);
		temp.rewind();
		ByteBuffer data = ByteBuffer.allocate(temp.capacity());
		//remove escape chars
		for(int i=0;i<sizeIn;i++){
			byte c = temp.get();
			if(	c == 0x7D ){
				c = temp.get();
				c ^= 0x20;
				i++;
				data.put(c);
			}else
				data.put(c);
		}
		
		data.rewind();
		
//		if ( !checksumOK(data) ){
//			Log.w(TAG, "Received Xbee frame with bad checksum");
//			return;
//		}
		
		data.get();//remove delimeter
		short frameSize = data.getShort();
		byte type = data.get();
		
		short addrSize;
		if(type == RX_64BIT)
			addrSize = 8;
		else if(type == RX_16BIT)
			addrSize = 2;
		else if( type == TX_STATUS)
		{
			byte frameACK = data.get();
			byte txStatus = data.get();
			if(txStatus != 0x00 )
				onTransmitResult(false,frameACK);
			else
				onTransmitResult(true, frameACK);
			return;
		}
		else{
			Log.w(TAG, "Zigbee packet type not known");
			return;
		}
		
		byte[] source = new byte[addrSize];
		data.get(source);

		byte rssi = data.get();
		byte opt = data.get();
		
		short payloadSize = (short) (frameSize-addrSize-3);

		if( payloadSize > 0)
		{
			byte[] payload = new byte[payloadSize];
			data.get(payload);
			mHnadService.onFrameReceived(new XbeeFrame(type,rssi,opt,source,payload));
		}
		else
			 Log.w(TAG, "Bad frame size received");
	}
}
