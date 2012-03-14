package cste.misc;

import android.content.Context;
import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import cste.messages.EventLogICD;

public class EventLogRowICD extends TableRow{
	protected TextView dateTime;
	protected TextView eventName;
	protected TextView ackNo;
	protected TextView eventData;
	
	/***
	 * Title row
	 * @param context
	 */
	public EventLogRowICD(Context context) {
		super(context);
		
		this.setBackgroundColor(Color.BLACK);
		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(1, 1, 1, 1);
		this.setLayoutParams(tableRowParams);

		dateTime = new TextView(context);
		dateTime.setText("Timestamp");
		dateTime.setTextSize(12);

        eventName = new TextView(context);
        eventName.setText("Event Type");
        eventName.setTextSize(12);

        ackNo = new TextView(context);
        ackNo.setText("AckNo");
        ackNo.setTextSize(12);
        
        eventData = new TextView(context);
        eventData.setText("Event Data");
        eventData.setTextSize(12);
        
        this.addView(dateTime);
        this.addView(eventName);
        this.addView(ackNo);
        this.addView(eventData);
	}
	
	public EventLogRowICD(Context context, EventLogICD logEntry) {
		super(context);
		
		this.setBackgroundColor(Color.BLACK);
		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(1, 1, 1, 1);
		this.setLayoutParams(tableRowParams);

		dateTime = new TextView(context);
		dateTime.setText(logEntry.timeStamp.toString() + " ");
		dateTime.setTextSize(12);

        eventName = new TextView(context);
        eventName.setText(logEntry.eventType.toString() + " ");
        eventName.setTextSize(12);

        ackNo = new TextView(context);
        ackNo.setText(String.valueOf(logEntry.ackNo) + " ");
        ackNo.setTextSize(12);
        
        eventData = new TextView(context);
        eventData.setText(logEntry.getStatusStr() + " ");
        eventData.setTextSize(12);
        
        this.addView(dateTime);
        this.addView(eventName);
        this.addView(ackNo);
        this.addView(eventData);
	}

}
