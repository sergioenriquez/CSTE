package cste.hnad;

public interface RadioCommInterface {
	boolean openDevice();
	void closeDevice();
	boolean transmit(byte[] message);
}
