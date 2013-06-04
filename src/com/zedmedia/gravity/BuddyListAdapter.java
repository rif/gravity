package com.zedmedia.gravity;

import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BuddyListAdapter extends ArrayAdapter<RosterEntry> {
	private List<RosterEntry> entries;

	public BuddyListAdapter(Context context, int resource,
			List<RosterEntry> items) {

		super(context, resource, items);

		this.entries = items;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.roster_list, null);
		}

		RosterEntry entry = entries.get(position);
		if (entry != null) {
			TextView statusText = (TextView) v.findViewById(R.id.status);
			TextView nameText = (TextView) v.findViewById(R.id.name);
			TextView groupText = (TextView) v.findViewById(R.id.group);
			if (statusText != null) {
				if (entry.getStatus() != null){
					statusText.setText(entry.getStatus().toString());
				}				
				String name = entry.getName();
				if (name == null || name.trim().equals("")) {
					name = entry.getUser();
				}
				nameText.setText(name);
				for (RosterGroup rg : entry.getGroups()) {
					GroupInfo gi = new GroupInfo(rg);
					groupText.setText(gi.getName());
					break;
				}
			}
		}

		return v;
	}
}