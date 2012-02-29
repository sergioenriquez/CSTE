package cste.hnad;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import cste.icd.SecurityDevice;
import android.os.Parcel;
import android.os.Parcelable;

/***
 * Parcelable version of the generic security device
 * @author User
 *
 */
public class Device extends SecurityDevice implements Parcelable{
	protected boolean visible;
	protected byte rssi;	
	
	public Device(DeviceUID devUID, DeviceType devType)
	{
		super(devUID, devType);
		visible = false;
		rssi = 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Device> CREATOR
    = new Parcelable.Creator<Device>() {
		public Device createFromParcel(Parcel in) {
		    return new Device(in);
		}
	
		public Device[] newArray(int size) {
		    return new Device[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(devUID.toString());
		dest.writeByte(devType.getBytes());
		dest.writeInt(rxAscension);
		dest.writeInt(txAscension);
		dest.writeByte(rssi);
		dest.writeString(sealID);
		dest.writeString(manifestID);
		dest.writeString(conveyanceID);
		dest.writeBooleanArray(new boolean[]{alarmOn,doorOpen,opMode});
		dest.writeValue(alarmOn);
		dest.writeValue(doorOpen);
		dest.writeValue(opMode);
	}
	
	private Device(Parcel in) {
		super(null,null);
		devUID = new DeviceUID(in.readString());
		devType = DeviceType.fromValue(in.readByte());
		rxAscension = in.readInt();
		txAscension = in.readInt();
		rssi = in.readByte();
		sealID = in.readString();
		manifestID = in.readString();
		conveyanceID = in.readString();
		boolean []b = new boolean[3];
		in.readBooleanArray(b);	
		alarmOn = b[0];
		doorOpen = b[1];
		opMode = b[2];
    }
}
