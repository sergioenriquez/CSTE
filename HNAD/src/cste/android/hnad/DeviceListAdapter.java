package cste.android.hnad;

import java.util.List;

import cste.android.R;
import cste.hnad.Device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<Device>{
	int resourceID;
	
	public DeviceListAdapter(Context context, int textViewResourceId,List<Device> objects) {
		super(context, textViewResourceId, objects);
		this.resourceID = textViewResourceId;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		Device item = getItem(position);
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resourceID, null);


        }
        
        CheckBox devSignal = (CheckBox)convertView.findViewById(R.id.devicevisible);
        devSignal.setChecked(item.visible);

        TextView devID = (TextView) convertView.findViewById(R.id.deviceid);
        devID.setText(item.id);
        
        TextView devType = (TextView) convertView.findViewById(R.id.devicetype);
        devType.setText(item.type);

        return convertView;
    }
}
