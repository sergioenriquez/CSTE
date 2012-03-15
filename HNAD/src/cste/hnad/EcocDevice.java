package cste.hnad;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import cste.components.ECoC;
import cste.icd.DeviceUID;

/***
 * Parcelable version of the ECoC, plus some signal data info
 * @author User
 *
 */
public class EcocDevice extends ECoC implements Parcelable{
	private static final String TAG = "EcocDevice";
	private static final long serialVersionUID = 2241983295406693238L;

	public EcocDevice(DeviceUID devUID){
		super(devUID);
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
		byte []content = serialize(this);
		if( content == null){
			Log.e(TAG,"Unable to serialize object");
			return;
		}
		int size = content.length;
		dest.writeInt(size);
		dest.writeByteArray(serialize(this));
	}
}
