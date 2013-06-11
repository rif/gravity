package com.zedmedia.gravity;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterGroup;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FeesDialog extends Dialog {
	private static final String TAG = "[gravity fees]";
	private ArrayList<GroupInfo> groups = new ArrayList<GroupInfo>();
	private ListView list;
	private Gravity mainActivity;
	private ArrayAdapter<GroupInfo> listAdapter;
	private Roster roster;

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
		roster = ServerConnection.getInstance().getConnection().getRoster();

		listAdapter = new ArrayAdapter<GroupInfo>(mainActivity, R.layout.list,
				groups);
		list.setAdapter(listAdapter);
		refreshList();
	}

	public void refreshList() {
		Log.d(TAG,"Refreshing fees: " + roster );
		if (roster != null) {
			groups.clear();
			for (RosterGroup rg : roster.getGroups()) {
				groups.add(new GroupInfo(rg));
			}
			listAdapter.notifyDataSetChanged();
		}
	}
}
