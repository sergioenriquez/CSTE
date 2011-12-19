package hnad.android.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * This is a two line ArrayAdapter. Instead of using the toString() method, this utilizes line1() and
 * line2() implemented by T (since it must implement TwoLineArrayAdapterItem). It uses Android's 
 * built-in layout two line list item layout where first line is bold.
 * 
 * @author Cory Sohrakoff
 *
 * @param <T>
 */
public class TwoLineArrayAdapter<T extends TwoLineArrayAdapterItem> extends ArrayAdapter<T> {

	public TwoLineArrayAdapter(Context context, List<T> objects) {
		// if we get a null list create one for super
		super(context, android.R.layout.two_line_list_item, (objects == null ? new ArrayList<T>() : objects));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// make sure view is loaded
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(android.R.layout.two_line_list_item, parent, false);

		}
		
		T item = getItem(position);
		if (item != null) {
			// First text view
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			if (textView != null) {
				String text = item.line1();
				if (text != null)
					textView.setText(text);
				else
					textView.setText("");
			}
			
			// Second text view
			textView = (TextView) convertView.findViewById(android.R.id.text2);
			if (textView != null) {
				String text = item.line2();
				if (text != null)
					textView.setText(text);
				else
					textView.setText("");
			}
		}		
		return convertView;
	}
}
