package cste.android.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import cste.android.R;
import cste.hnad.Device;

public class DeviceListAdapter extends ArrayAdapter<Device>{
	int resourceID;
	
	public DeviceListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.resourceID = textViewResourceId;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		Device dev = getItem(position);
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resourceID, null);
        }
        
        CheckBox devSignal = (CheckBox)convertView.findViewById(R.id.devicevisible);
        devSignal.setChecked(dev.visible);

        TextView devID = (TextView) convertView.findViewById(R.id.deviceid);
        devID.setText(dev.devUID.toString());
        
        TextView devType = (TextView) convertView.findViewById(R.id.devicetype);
        devType.setText(dev.devType.toString());

        return convertView;
    }
}
