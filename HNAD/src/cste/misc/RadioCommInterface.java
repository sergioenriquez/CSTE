package cste.misc;

public interface RadioCommInterface {
	boolean openDevice();
	void closeDevice();
	boolean transmit(byte[] message);
}
