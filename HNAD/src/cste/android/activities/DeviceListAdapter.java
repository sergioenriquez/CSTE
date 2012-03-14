package cste.android.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cste.android.R;
import cste.components.ComModule;

public class DeviceListAdapter extends ArrayAdapter<ComModule>{
	int resourceID;
	
	public DeviceListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.resourceID = textViewResourceId;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ComModule dev = getItem(position);
		
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resourceID, null);
        }
        
        ImageView signalImg = (ImageView)convertView.findViewById(R.id.devicevisible);
        if(!dev.inRange)
        	signalImg.setImageResource(R.drawable.stat_sys_signal_null);
        else if(dev.rssi >= 90)
        	signalImg.setImageResource(R.drawable.stat_sys_signal_0);
        else if(dev.rssi >= 60)
        	signalImg.setImageResource(R.drawable.stat_sys_signal_1);
        else if(dev.rssi >= 45)
        	signalImg.setImageResource(R.drawable.stat_sys_signal_2);
        else if(dev.rssi >= 30)
        	signalImg.setImageResource(R.drawable.stat_sys_signal_3);
        else 
        	signalImg.setImageResource(R.drawable.stat_sys_signal_4);
        
        TextView devType = (TextView) convertView.findViewById(R.id.deviceid);
        devType.setText(dev.UID().toString());
        
        TextView devID = (TextView) convertView.findViewById(R.id.devicetype);
        devID.setText(dev.devType().toString());
        
        ImageView keyIcon = (ImageView)convertView.findViewById(R.id.haveKey);
        if( dev.haveKey()  )
        	keyIcon.setImageResource(R.drawable.key);
        else
        	keyIcon.setImageResource(R.drawable.blank);
        
        ImageView armedIcon = (ImageView)convertView.findViewById(R.id.devArmed);
 
        if( !dev.getArmedStatus() )
        	armedIcon.setImageResource(R.drawable.lock_off);
        else 
        	armedIcon.setImageResource(R.drawable.lock_on);
        
        TextView devProblem = (TextView) convertView.findViewById(R.id.devProblem);
        int errorCount = dev.getAlarmCount();
        if( errorCount > 0 ){
        	devProblem.setText( String.valueOf( dev.getAlarmCount()) );
        	//devProblem.setBackgroundResource(R.drawable.alarm);
        }else
        	devProblem.setText("");

        return convertView;
    }
}
