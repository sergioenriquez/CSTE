package cste.misc;

public class XbeeFrame {
	public final byte rssi;
	public final byte type;
	public final byte opt;
	public final byte[] address;
	public final byte[] payload;
	
	public final byte frameACK;
	public final byte statusCode;
	
	/***
	 * Place holder frame with an unparsed type
	 * @param type
	 */
	public XbeeFrame(byte type)
	{
		this.type = type;
		this.rssi = 0;
		this.opt = 0;
		this.address = null;
		this.payload = null;
		frameACK = 0;
		statusCode = 4;
	}
	
	public XbeeFrame(byte type, byte frameACK, byte statusCode)
	{
		this.type = type;
		this.rssi = 0;
		this.opt = 0;
		this.address = null;
		this.payload = null;
		
		this.frameACK = frameACK;
		this.statusCode = statusCode;
	}
	
	public XbeeFrame(
			byte type,
			byte rssi,
			byte opt,
			byte[] sourceAddrs,
			byte[] payload)
	{
		this.type = type;
		this.rssi = rssi;
		this.opt = opt;
		this.address = sourceAddrs;
		this.payload = payload;
		frameACK = 0;
		statusCode = 4;
	}
}
