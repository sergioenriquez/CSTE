package cste.misc;

import cste.icd.HnadEventLog;
import cste.messages.EventLogICD;
import cste.messages.IcdMsg;
import android.content.Context;
import android.graphics.Color;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class EventLogRowHnad extends TableRow{
	protected TextView dateTime;
	protected TextView eventName;
	protected TextView userName;
	protected TextView logID;
	protected TextView msgDest;
	protected TextView msgComd;
	
	public EventLogRowHnad(Context context) {
		super(context);
	
		this.setBackgroundColor(Color.BLACK);
		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
	            LayoutParams.FILL_PARENT,
	            LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(1, 1, 1, 1);
		this.setLayoutParams(tableRowParams);
		
		logID = new TextView(context);
	    logID.setText("Log ID");
	    logID.setTextSize(12);
	
		dateTime = new TextView(context);
		dateTime.setText("Timestamp");
		dateTime.setTextSize(12);
		
		userName = new TextView(context);
		userName.setText("Username");
		userName.setTextSize(12);
	
	    eventName = new TextView(context);
	    eventName.setText("Event Type");
	    eventName.setTextSize(12);
	    
	    msgDest = new TextView(context);
	    msgDest.setText("Destination");
	    msgDest.setTextSize(12);
	    
	    msgComd = new TextView(context);
	    msgComd.setText("Cmd Type");
	    msgComd.setTextSize(12);
	    
	    this.addView(logID);
	    this.addView(dateTime);
	    this.addView(userName);
	    this.addView(eventName);
	    this.addView(msgDest);
	    this.addView(msgComd);
	}
	
	public EventLogRowHnad(Context context, HnadEventLog logEntry) {
		super(context);
		
		this.setBackgroundColor(Color.BLACK);
		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(1, 1, 1, 1);
		this.setLayoutParams(tableRowParams);

		logID = new TextView(context);
        logID.setText(String.valueOf(logEntry.logID) + " ");
        logID.setTextSize(12);
        
		dateTime = new TextView(context);
		dateTime.setText(logEntry.timeStamp.toString() + " ");
		dateTime.setTextSize(12);
	
		userName = new TextView(context);
		userName.setText(logEntry.username.toString() + " ");
		userName.setTextSize(12);
		
        eventName = new TextView(context);
        eventName.setText(logEntry.eventType.toString() + " ");
        eventName.setTextSize(12);

        msgDest = new TextView(context);
        if(logEntry.msgSent.msgStatus == IcdMsg.MsgStatus.OK)
	        msgDest.setText(logEntry.msgSent.destUID.toString() + " ");
        else
	        msgDest.setText("N/A");
        msgDest.setTextSize(12);
        
        msgComd = new TextView(context);
        if(logEntry.msgSent.msgStatus == IcdMsg.MsgStatus.OK)
        	msgComd.setText(logEntry.msgSent.headerData.msgType.toString());
        else
        	msgComd.setText("N/A");
        msgComd.setTextSize(12);
        
        this.addView(logID);
        this.addView(dateTime);
        this.addView(userName);
        this.addView(eventName);
        this.addView(msgDest);
        this.addView(msgComd);
	}
}
