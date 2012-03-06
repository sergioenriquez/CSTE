package cste.misc;

import static cste.icd.Utility.strToHex;

import java.nio.ByteBuffer;
import java.util.Hashtable;

import cste.android.core.HNADService;
import cste.components.ComModule;
import cste.hnad.RadioCommInterface;
import android.util.Log;

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
	
	private static RadioCommInterface comInterface;
	private static HNADService service; // TODO replace with interface

	protected static byte nextFrameAck = 0;
	protected static Hashtable<Byte,RetryTxItem> txTable = new Hashtable<Byte, RetryTxItem>();
	
	public static final int MAX_RETRY_ATTEMPTS = 3;
	public static final int TIMEOUT_PERIOD = 500;

	//BCAST
	
	/***
	 * 
	 */
	public static void setRadioInterface(RadioCommInterface radioInterface){
		comInterface = radioInterface;
	}
	
	public static void onTransmitResult(boolean success, byte frameAck){
		RetryTxItem item = txTable.get(frameAck);
		if( item == null){
			Log.e(TAG,"Retransmit table item is missing");
			return;
		}
		item.clearTimer();
		
		if( success )
			service.onRadioTransmitResult(true);
		else{
			if(item.retryAttempts < MAX_RETRY_ATTEMPTS)
			{
				Log.i(TAG,"No ACK received, retrying");
				comInterface.transmit(item.payload);
				item.retryAttempts++;
				item.restartTimer();
			}else{
				service.onRadioTransmitResult(false);
			}
		}
	}
	
	public static void transmitPkt(byte []dest, byte []payload){
		boolean needAck = dest.equals(BROADCAST_ADDRESS) ? false : true;
		
		byte[] frame = buildFrame(dest,payload,needAck);

		if( comInterface.transmit(frame) && needAck){
			txTable.put(nextFrameAck,  new RetryTxItem(nextFrameAck, frame) );
			nextFrameAck++;
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

	//TODO handle other msg types
	/***
	 * Unwraps the data delivered by the Zigbee transceiver
	 * @param msg
	 * @return
	 */
	public static void parseFrame(byte[] msg)
	{
		ByteBuffer temp = ByteBuffer.wrap(msg);
		ByteBuffer data = ByteBuffer.allocate(temp.capacity());
		for(int i=0;i<temp.capacity();i++)
		{
			byte c = temp.get();
			if(	c == 0x7D )
			{
				c = temp.get();
				c ^= 0x20;
				i++;
				data.put(c);
			}
			else
				data.put(c);
		}
		data.rewind();
		data.get();//delimeter
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
			Log.e(TAG, "Zigbee packet type not known");
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
			service.onFrameReceived(new XbeeFrame(type,rssi,opt,source,payload));
		}
		else
			 Log.w(TAG, "Bad frame size received");
	}
}
