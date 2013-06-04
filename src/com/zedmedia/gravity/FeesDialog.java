package com.zedmedia.gravity;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterGroup;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FeesDialog extends Dialog {
	private ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
	private ListView list;
	private Gravity mainActivity;

	public FeesDialog(Gravity gravity) {
		super(gravity);
		mainActivity = gravity;
	}

	@Override
	protected void onStart() {
		super.onStart();
		setContentView(R.layout.edit_fees);
		setTitle("Edit fees");
		list = (ListView) this.findViewById(R.id.feesList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				GroupDialog gd = new GroupDialog(mainActivity);
				gd.show();
				gd.setGroupInfo(groups.get(position));
			}

		});
		Roster roster = ServerConnection.getInstance().getConnection()
				.getRoster();
		groups.clear();
		for (RosterGroup rg : roster.getGroups()) {
			groups.add(new GroupInfo(rg));
		}
		ArrayAdapter<GroupInfo> adapter = new ArrayAdapter<GroupInfo>(
				mainActivity, R.layout.list, groups);
		list.setAdapter(adapter);
	}

}
