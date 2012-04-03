package hnad.android.ListAdapter;

import hnad.android.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Parameter T must implement {@link ImageArrayAdapterItem} and {@link TwoLineArrayAdapterItem}.
 * @author Cory Sohrakoff
 *
 * @param <T>
 */
public class TwoLineWithImageArrayAdapter<T extends ImageArrayAdapterItem & TwoLineArrayAdapterItem> extends ArrayAdapter<T> {	
	
	/**
	 * 
	 * @param context
	 * @param resource
	 * @param textViewResourceId
	 * @param objects
	 */
	public TwoLineWithImageArrayAdapter(Context context, ArrayList<T> objects) {
		super(context, R.layout.two_line_with_image_list_item, (objects == null ? new ArrayList<T>() : objects));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.two_line_with_image_list_item, parent, false);

		}
		T item = getItem(position);
		if (item != null) {
			// line 1
			TextView tv = (TextView) v.findViewById(R.id.line_1);
			if (tv != null) {
				tv.setText(item.line1());
			}
			
			// line 2
			tv = (TextView) v.findViewById(R.id.line_2);
			if (tv != null) {
				tv.setText(item.line2());
			}
			
			// image
			ImageView iv = (ImageView) v.findViewById(R.id.image);
			if (iv != null) {
				iv.setImageResource(item.getImageResource());
			}
		}		
		return v;
	}
	
	

}
