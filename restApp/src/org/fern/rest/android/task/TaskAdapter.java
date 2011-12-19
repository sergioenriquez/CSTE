package org.fern.rest.android.task;

import org.fern.rest.android.ArrayAdapter;
import org.fern.rest.android.R;
import org.fern.rest.android.RestApplication.Priority;

import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TaskAdapter extends ArrayAdapter<Task> {
    private static final String TAG = "TaskAdapter";
    public static final int NAME = 0;
    public static final int ESTIMATED_COMPLETION_TIME = 1;
    public static final int PROGRESS = 2;
    public static final int PRIORITY = 3;
    public static final int EXPIRATION_DATE = 4;
    public static final int START_DATE = 5;
    public static final int LAST_MODIFIED_DATE = 6;
    public static final int ADDED_DATE = 7;
    private int mResource;
    private int mLastCompareType = -1;
    private int mSortDirection = 1;

    /**
     * @see org.fern.rest.android.ArrayAdapter;
     */
    public TaskAdapter(Context context, int textViewResourceId,
            List<Task> objects) {
        super(context, textViewResourceId, objects);
        this.mResource = textViewResourceId;
    }

    /**
     * @see org.fern.rest.android.ArrayAdapter;
     */
    public TaskAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mResource = textViewResourceId;
    }

    /**
     * Function to set of the view for each list item.
     * 
     * @see android.widget.ArrayAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.mResource, null);
        }
        try {
            if (convertView == null) { throw new NullPointerException(
                    "View passed into getView was empty!"); }
        } catch (NullPointerException npe) {
            Log.wtf(TAG, "NullPointerException", npe);
        }

        TextView name = (TextView) convertView.findViewById(R.id.taskItemName);
        ImageView priority = (ImageView) convertView
                .findViewById(R.id.taskItemPriority);
        ProgressBar progress = (ProgressBar) convertView
                .findViewById(R.id.taskItemProgressBar);

        Task task = this.getItem(position);

        ClipDrawable clip = (ClipDrawable) ((LayerDrawable) progress
                .getProgressDrawable())
                .findDrawableByLayerId(android.R.id.progress);
        clip.setColorFilter(getColorFromProgress(task.getProgress()),
                PorterDuff.Mode.SRC_OVER);
        name.setText(task.getName());
        priority.setImageResource(getDrawableResFromPriority(task.getPriority()));

        ((GradientDrawable) priority.getDrawable()).setColor(getColorFromPriority(task.getPriority()));
        progress.setProgress(task.getProgress());
        return convertView;
    }

    private int getDrawableResFromPriority(Priority p) {
        return p.getResId();
    }

    private int getColorFromProgress(int progress) {
        float[] hsv = new float[3];
        hsv[0] = (((float) progress) / 100) * 120; // hue
        hsv[1] = 1; // saturation
        hsv[2] = 1; // value
        return Color.HSVToColor(0xff, hsv);
    }
    
    private int getColorFromPriority(Priority priority) {
        float pri = -1;
        switch (priority) {
            case HIGHEST: pri = 0; break;
            case VERYHIGH: pri = 1; break;
            case HIGH: pri = 3; break;
            case MEDIUM: pri = 4; break;
            case LOW: pri = 5; break;
            case VERYLOW: pri = 6; break;
            case LOWEST: pri = 7; break;
        }
        if (pri == -1) { return 0xAAAAAAAA; }
        float[] hsv = new float[3];
        hsv[0] = ((pri) / 7) * 120;
        hsv[1] = 1;
        hsv[2] = 1;
        return Color.HSVToColor(0xff, hsv);
    }

    public void sortList(int sortMethod) {
        if (sortMethod == mLastCompareType) {
            mSortDirection = -mSortDirection;
        } else {
            mSortDirection = 1;
        }
        sort(new TaskComparator(sortMethod, mSortDirection));
        mLastCompareType = sortMethod;
    }

    private static class TaskComparator implements Comparator<Task> {
        private int mCompareType;
        private int mDirection;

        public TaskComparator(int compareType, int sortDirection) {
            mCompareType = compareType;
            mDirection = sortDirection;
        }

        @Override
        public int compare(Task object1, Task object2) {
            int compareValue = 0;
            switch (mCompareType) {
                case NAME:
                    compareValue = object1.getName().compareToIgnoreCase(
                            object2.getName());
                    break;
                case ESTIMATED_COMPLETION_TIME:
                    compareValue = object1.getEstimatedCompletionTime().compareTo(
                            object2.getEstimatedCompletionTime());
                    break;
                case PROGRESS:
                    compareValue = object1.getProgress().compareTo(
                            object2.getProgress());
                    break;
                case PRIORITY:
                    if (object1.getName().equals("Derp") || object2.getName().equals("Derp")) {
                        int ordinal1 = object1.getPriority().ordinal();
                        int ordinal2 = object2.getPriority().ordinal();
                        compareValue = ordinal1 + ordinal2;
                    }
                    compareValue = new Integer(object1.getPriority().ordinal())
                            .compareTo(new Integer(object2.getPriority()
                                    .ordinal()));
                    break;
                case EXPIRATION_DATE:
                    compareValue = object1.getExpirationDate().compareTo(object2.getExpirationDate());
                    break;
                case START_DATE:
                    compareValue = object1.getActivatedDate().compareTo(object2.getActivatedDate());
                    break;
                case LAST_MODIFIED_DATE:
                    compareValue = object1.getModifiedDate().compareTo(object2.getModifiedDate());
                    break;
                case ADDED_DATE:
                    compareValue = object1.getAdditionDate().compareTo(object2.getAdditionDate());
                    break;
            }
            return mDirection * compareValue;
        }
    }
}
