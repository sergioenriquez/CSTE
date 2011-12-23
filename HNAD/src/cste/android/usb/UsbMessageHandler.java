package cste.android.usb;

import android.os.Handler;
import android.os.Message;

public class UsbMessageHandler extends Handler {
	public static final int MESSAGE_SWITCH = 1;
	public static final int MESSAGE_TEMPERATURE = 2;
	public static final int MESSAGE_LIGHT = 3;
	public static final int MESSAGE_JOY = 4;

	public static final byte LED_SERVO_COMMAND = 2;
	public static final byte RELAY_COMMAND = 3;
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_SWITCH:
			//SwitchMsg o = (SwitchMsg) msg.obj;
			//handleSwitchMessage(o);
			break;

		case MESSAGE_TEMPERATURE:
			//TemperatureMsg t = (TemperatureMsg) msg.obj;
			//handleTemperatureMessage(t);
			break;

		case MESSAGE_LIGHT:
			int x = (Integer) msg.obj;
			//handleLightMessage(l);
			break;

		case MESSAGE_JOY:
			//JoyMsg j = (JoyMsg) msg.obj;
			//handleJoyMessage(j);
			break;

		}
	}

}