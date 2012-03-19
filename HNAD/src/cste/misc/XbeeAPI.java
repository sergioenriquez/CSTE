package cste.misc;

import static cste.icd.general.Utility.strToHex;

import java.nio.ByteBuffer;
import java.util.Hashtable;

import android.util.Log;
import android.widget.Toast;
import cste.android.core.HNADService;
import cste.hnad.RadioCommInterface;

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
	public static HNADService mHnadService; // TODO replace with interface

	protected static byte nextFrameAck = 0;
	protected static Hashtable<Byte,XbeeTxItem> txTable = new Hashtable<Byte, XbeeTxItem>();
	
	public static final int MAX_RETRY_ATTEMPTS = 3;
	public static final int TIMEOUT_PERIOD = 1000;

	/***
	 * 
	 */
	public static void setRadioInterface(RadioCommInterface radioInterface){
		comInterface = radioInterface;
	}
	
	public static void setHnadService(HNADService mHnadService){
		XbeeAPI.mHnadService = mHnadService;
	}
	
	public static synchronized void onTransmitResult(boolean success, byte frameAck){
		XbeeTxItem item = txTable.get(frameAck);
		if( item == null){
			Log.w(TAG,"Retransmit table item not found");
			return;
		}
		item.clearTimer();
		
		if( success ){
			txTable.remove(frameAck);
			//mHnadService.onRadioTransmitResult(true,item.destination);
		}
		else{
			if(item.retryAttempts < MAX_RETRY_ATTEMPTS){
				Log.i(TAG,"No ACK received, retrying");
				comInterface.transmit(item.payload);
				item.retryAttempts++;
				item.restartTimer();
			}else{
				txTable.remove(frameAck);
				//mHnadService.onRadioTransmitResult(false,item.destination);
				//Toast.makeText(mHnadService.getContext(), "No reply received", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/***
	 * Try to send a packet to the specified destination. If the destination is not a broadcast address
	 * then it will keep retrying until a MAC layer ACK is received or the try counter is exceeded.
	 * @param dest
	 * @param payload
	 * @return
	 */
	public static synchronized boolean transmitPkt(byte []dest, byte []payload){
		if( dest == null || payload == null){
			Log.e(TAG,"Tried to transmit a null packet");
			//mHnadService.onRadioTransmitResult(false,dest);
			return false;
		}
		
		boolean needAck = dest.equals(BROADCAST_ADDRESS) ? false : true;
		
		byte[] frame = buildFrame(dest,payload,needAck);
		boolean txResult = comInterface.transmit(frame);
		
		if( needAck ){
			if ( txResult ){
				txTable.put(nextFrameAck,  new XbeeTxItem(nextFrameAck, frame, dest) );
				nextFrameAck++;
			}else{
				Toast.makeText(mHnadService.getContext(), "USB interface not availible", Toast.LENGTH_SHORT).show();
				//mHnadService.onRadioTransmitResult(false,dest);
			}
		}
		return txResult;
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
		for(int s=0; s<tmp.capacity() ; s++,newSize++){
			byte c = tmp.get(s);
			if( c == 0x7E || 
				c == 0x7D ||
				c == 0x11 ||
				c == 0x13){
				escapedSeq.put((byte)0x7D);
				escapedSeq.put((byte) (c ^ 0x20));
				newSize++;
			}else
				escapedSeq.put(c);
		}
		byte []zigbeePkt = new byte[newSize];
		escapedSeq.rewind();
		escapedSeq.get(zigbeePkt);
		
		return zigbeePkt;
	}
	
	private static boolean checksumOK(ByteBuffer buffer){
		buffer.rewind();
		int i;
		byte sum = 0;
		for(i=0;i<buffer.capacity()-1;i++)
			sum += buffer.get();

		byte calcCheckSum = (byte) (0xFF  - sum);
		byte checksum = buffer.get();
		if ( calcCheckSum == checksum)
			return true;
		else{
			Log.w(TAG, "Received Xbee frame with bad checksum");
			return false;
		}
	}
	
	protected static void processFrame(ByteBuffer buffer, short frameSize){
		ByteBuffer data = ByteBuffer.allocate(frameSize+2);
		
		//remove escape chars
		for(int i=0;i<frameSize+2;i++){
			byte c = buffer.get();
			if(	c == 0x7D ){
				c = buffer.get();
				c ^= 0x20;
				i++;
				data.put(c);
			}else
				data.put(c);
		}
		data.put(buffer.get());
		if ( !checksumOK(data) )
			return;
		
		data.rewind();
		byte type = data.get();
		
		short addrSize;
		if(type == RX_64BIT)
			addrSize = 8;
		else if( type == TX_STATUS){
			byte frameACK = data.get();
			byte txStatus = data.get();
			if(txStatus != 0x00 )
				onTransmitResult(false,frameACK);
			else
				onTransmitResult(true, frameACK);
			return;
		}
		else{
			Log.w(TAG, "Xbee packet type not processed: " + Byte.toString(type));
			return;
		}
		
		byte[] source = new byte[addrSize];
		data.get(source);

		byte rssi = data.get();
		byte opt = data.get();
		
		int payloadSize = frameSize-addrSize-3;
		byte[] payload = new byte[payloadSize];
		data.get(payload);
		mHnadService.onFrameReceived(new XbeeFrame(type,rssi,opt,source,payload));
	}
	

	//TODO handle other msg types
	/***
	 * Unwraps the data delivered by the Zigbee transceiver
	 * @param msg
	 * @return
	 */
	public static void parseFrame(byte[] msg,int msgSize){
		if( msgSize < 3){
			Log.w(TAG, "Received a frame with bad size");
			return;
		}

		ByteBuffer temp = ByteBuffer.wrap(msg);
		short frameSize;
		byte delim;
		while(temp.position() < msgSize){
			delim = temp.get();
			if( delim != DELIMETER){
				//Log.w(TAG, "Did not receive a valid Xbee frame");
				//break;
				continue;
			}
				
			frameSize = temp.getShort();
			if( temp.remaining() < frameSize){
				Log.w(TAG, "Received a frame with bad size");
				break;
			}
			processFrame(temp,frameSize);
		}
	}
}
