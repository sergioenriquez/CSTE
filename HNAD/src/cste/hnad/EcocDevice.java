package cste.hnad;

import android.os.Parcel;
import android.os.Parcelable;
import cste.components.ECoC;
import cste.icd.DeviceType;
import cste.icd.DeviceUID;

/***
 * Parcelable version of the ECoC, plus some signal data info
 * @author User
 *
 */
public class EcocDevice extends ECoC implements Parcelable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2241983295406693238L;
	public boolean visible;
	public byte rssi;	
	
	public EcocDevice(DeviceUID devUID)
	{
		super(devUID);
		visible = false;
		rssi = 100;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<EcocDevice> CREATOR
    = new Parcelable.Creator<EcocDevice>() {
		public EcocDevice createFromParcel(Parcel in) {
			
			int size = in.readInt();
			byte []content = new byte[size];
			in.readByteArray(content);
			
		    return(EcocDevice)deserialize(content);
		}
	
		public EcocDevice[] newArray(int size) {
		    return new EcocDevice[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//dest.writeString(devUID.toString());
		//dest.writeByte(devType.getBytes());
		//dest.writeInt(rxAscension);
		//dest.writeInt(txAscension);
		//dest.writeByte(rssi);
		byte []content = serialize(this);
		int size = content.length;
		dest.writeInt(size);
		dest.writeByteArray(serialize(this));
		//dest.writeByte(errorCode);
		//dest.writeByteArray(statusData);
	}
}
