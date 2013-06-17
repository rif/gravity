package com.zedmedia.gravity;

import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BuddyListAdapter extends ArrayAdapter<GravityRosterEntry> {
	private List<GravityRosterEntry> entries;

	public BuddyListAdapter(Context context, int resource,
			List<GravityRosterEntry> items) {

		super(context, resource, items);

		this.entries = items;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		Roster roster = ServerConnection.getInstance().getConnection()
				.getRoster();

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.roster_list, null);
		}

		GravityRosterEntry entry = entries.get(position);
		if (entry != null) {
			TextView statusText = (TextView) v.findViewById(R.id.status);
			TextView nameText = (TextView) v.findViewById(R.id.name);
			TextView groupText = (TextView) v.findViewById(R.id.group);
			if (statusText != null) {
				Presence p = roster.getPresence(entry.getRosterEntry()
						.getUser());
				statusText.setText("");
				statusText.setTag(p.toString());
				statusText.setText(statusText.getText().toString() + " "
						+ entry.getFee());
				String name = entry.getName();
				if (name == null || name.trim().equals("")) {
					name = entry.getRosterEntry().getUser();
				}
				nameText.setText(name);
				for (RosterGroup rg : entry.getRosterEntry().getGroups()) {
					GroupInfo gi = new GroupInfo(rg);
					groupText.setText(gi.getName());
					break;
				}
			}
		}

		return v;
	}
}