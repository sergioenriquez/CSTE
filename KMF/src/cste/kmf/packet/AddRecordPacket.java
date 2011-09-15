package cste.kmf.packet;
import static cste.kmf.packet.PacketTypes.*;

public class AddRecordPacket {
	protected final byte type = ADD_RECORD;
	protected byte uid[] = new byte[4];
	protected byte rekeyKey[] = new byte[16];
	
	public void writeToSocket()
	{
		
	}
}
