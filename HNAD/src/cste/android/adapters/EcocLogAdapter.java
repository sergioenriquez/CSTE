package cste.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cste.android.R;
import cste.icd.icd_messages.EventLogICD;

public class EcocLogAdapter extends ArrayAdapter<EventLogICD> {
	static final String TAG = "ECOC log adapter";
	int resourceID;
	
	static int logRec = 1;
	
	public EcocLogAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.resourceID = textViewResourceId;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		EventLogICD logEntry = getItem(position);
		
		
		
		if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resourceID, null);
        }
		
		TextView id = (TextView) convertView.findViewById(R.id.logID);
		//id.setText(String.valueOf(logEntry.logRecordNum));
		id.setText(String.valueOf(logRec++));
		
		TextView dateTime = (TextView) convertView.findViewById(R.id.logTime);
		dateTime.setText(logEntry.timeStamp.toString());
		
		TextView eventName = (TextView) convertView.findViewById(R.id.logType);
		eventName.setText(logEntry.eventType.toString());

		TextView eventData = (TextView) convertView.findViewById(R.id.logData);
		eventData.setText(logEntry.getStatusStr());

        return convertView;
    }
}
