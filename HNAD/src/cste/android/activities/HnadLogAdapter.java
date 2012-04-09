package cste.android.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cste.android.R;
import cste.misc.HnadEventLog;

public class HnadLogAdapter extends ArrayAdapter<HnadEventLog>{
	int resourceID;
	
	public HnadLogAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.resourceID = textViewResourceId;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		HnadEventLog logEntry = getItem(position);
		
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resourceID, null);
        }
        
        TextView id = (TextView)convertView.findViewById(R.id.logID);
        id.setText(String.valueOf(logEntry.logID));
        
        TextView time = (TextView)convertView.findViewById(R.id.logTime);
        time.setText(logEntry.timeStamp.toString());
        
        TextView user = (TextView)convertView.findViewById(R.id.logUser);
        user.setText(logEntry.username.toString());
        
        TextView event = (TextView)convertView.findViewById(R.id.logEvent);
        event.setText(logEntry.eventType.toString());
        
        TextView dest = (TextView)convertView.findViewById(R.id.logDest);
        if( logEntry.msgSent.destUID != null)
        	dest.setText(logEntry.msgSent.destUID.toString());
        else
        	dest.setText("N/A");
        
        TextView cmd = (TextView)convertView.findViewById(R.id.logCmd);
        if( logEntry.msgSent.header != null)
        	cmd.setText(logEntry.msgSent.header.msgType.toString());
        else
        	cmd.setText("N/A");
        
        return convertView;
    }
}
