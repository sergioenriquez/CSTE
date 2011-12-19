package hnad.android.Service;

import hnad.android.ICD.ICD;
import hnad.android.Miscellaneous.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;

/**
 * This class implements the {@link AndroNadService} for UDP (via Wifi in our case). You can
 * follow this class as an example of how to implement a subclass of AndroNadService.
 * 
 * Note: This class only expects packets to be coming from one source. Accordingly, it only saves the last
 * 		 packet source's IP address as the recipient for outgoing packets.
 * 
 * @author Cory Sohrakoff
 *
 */
public class UdpAndroNadService extends AndroNadService {
	// For debugging
	private static final String TAG = UdpAndroNadService.class.getName();
	private static final boolean D = true;
	
	/**
	 * Reference to the UDP connection class.
	 */
	private UdpConnectionThread mUdpConnectionThread = null;

	@Override
	protected synchronized void connect() {		
		super.connect();
		mUdpConnectionThread = new UdpConnectionThread();
		mUdpConnectionThread.start();
	}

	@Override
	protected synchronized void stopThreads() {
		if (mUdpConnectionThread != null) {
			mUdpConnectionThread.cancel(); 
			mUdpConnectionThread = null;
		}
		super.stopThreads();
	}

	@Override
	public void sendMessage(String destinationUID, byte[] message) {
		// prepend destinationUID to message, according to DC-Lite code.
		byte[] temp = new byte[ICD.UID_LENGTH + message.length];
		Utils.hexStringToBytes(destinationUID, temp, 0);
		System.arraycopy(message, 0, temp, ICD.UID_LENGTH, message.length);
		message = temp;
		
        // Create temporary object
        UdpConnectionThread thread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (getState() != STATE_CONNECTED) return;
            thread = mUdpConnectionThread;
        }       
        // Perform the write unsynchronized
        thread.sendMessage(message);
	}
	
	private class UdpConnectionThread extends Thread {
		/**
		 * UDP port this will be running on.
		 */
		private static final int UDP_PORT = 11000;
		
		/**
		 * UDP packet buffer size
		 */
		private static final int UDP_PACKET_BUFFER_SIZE = 1024;
		
		/**
		 * Address to send packets to.
		 */
		private String recipientIpAddress = "192.168.1.50"; // default to this since that's what
																  // the reference NAD is programmed to
		
		/**
		 * The socket to send/receive datagrams.
		 */
		private DatagramSocket mmDatagramSocket;
		
		public UdpConnectionThread() {
			try {
				mmDatagramSocket = new DatagramSocket(UDP_PORT);
			} catch (SocketException e) {
				connectionFailed();
			}
		}
		
		@Override
		public void run() {
			// mmDatagramSocket is null that means connectionFailed has been called so quit
			if (mmDatagramSocket == null)
				return;
			
			if (D) Log.d(TAG, "UdpConnectionThread is running...");
			connected(); // call connected when running
			
			byte[] buffer = new byte[UDP_PACKET_BUFFER_SIZE];
			DatagramPacket packet;
			while (true) {
				packet = new DatagramPacket(buffer, buffer.length);
				try {
					mmDatagramSocket.receive(packet);
					// get ip address from packet
					recipientIpAddress = packet.getAddress().getHostAddress();
					if (D) Log.i(TAG, "UDP packet received from " + recipientIpAddress);
					
					// copy data from buffer
					byte[] data = new byte[packet.getLength()];
					System.arraycopy(packet.getData(), 0, data, 0, data.length);
					parseMessage(data, data.length);
				} catch (IOException e) {
					connectionTerminated();
					return;
				}
			}
		}

		public void sendMessage(byte[] message) {
			try {
				DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getByName(recipientIpAddress), UDP_PORT);
				if (D) Log.i(TAG, "Sending UDP packet to " + recipientIpAddress);
				mmDatagramSocket.send(packet);
			} catch (UnknownHostException e) {
				Log.e(TAG, "Could not get InetAddress for " + recipientIpAddress, e);
			} catch (IOException e) {
	            Log.e(TAG, "Exception while sending message", e);
			}
		}
		
		public void cancel() {
			if (mmDatagramSocket != null)
				mmDatagramSocket.close();
		}
	}
}
