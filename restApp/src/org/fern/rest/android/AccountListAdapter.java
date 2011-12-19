package org.fern.rest.android;

import org.fern.rest.android.user.User;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AccountListAdapter extends ArrayAdapter<User> {
    int resource;

    public AccountListAdapter(Context context, int textViewResourceId,
            List<User> objects) {
        super(context, textViewResourceId, objects);
        this.resource = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User item = getItem(position);
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(this.resource, null);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.accountItemName);
        tv.setText(item.getName());

        return convertView;
    }
}
