package cste.misc;

public class ZigbeeFrame {
	public final int rssi;
	public final byte type;
	public final byte opt;
	public final byte[] sourceAddrs;
	public final byte[] payload;
	
	/***
	 * Place holder frame with an unparsed type
	 * @param type
	 */
	public ZigbeeFrame(byte type)
	{
		this.type = type;
		this.rssi = 0;
		this.opt = 0;
		this.sourceAddrs = null;
		this.payload = null;
	}
	
	public ZigbeeFrame(
			byte type,
			int rssi,
			byte opt,
			byte[] sourceAddrs,
			byte[] payload)
	{
		this.type = type;
		this.rssi = rssi;
		this.opt = opt;
		this.sourceAddrs = sourceAddrs;
		this.payload = payload;
	}
}