package cste.hnad;

import cste.icd.DeviceType;
import cste.icd.DeviceUID;
import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable{
	
	public DeviceUID devUID;
	public DeviceType devType;
	public int msgAsc;
	public byte rssi;	
	
	public String sealID;
	public String manifestID;
	public String conveyanceID;
	
	public boolean visible;
	boolean alarmOn;
	boolean doorOpen;
	boolean opMode;
	
	public Device(DeviceUID devUID, DeviceType devType ){
		this.devUID = devUID;
		this.devType = devType;
		this.msgAsc = 0;
		this.rssi = 0;
		this.sealID = "NA1";
		this.manifestID = "NA2";
		this.conveyanceID = "NA3";
		this.alarmOn = false;
		this.doorOpen = false;
		this.opMode = false;
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
		dest.writeInt(msgAsc);
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
		devUID = new DeviceUID(in.readString());
		devType = DeviceType.fromValue(in.readByte());
		msgAsc = in.readInt();
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
