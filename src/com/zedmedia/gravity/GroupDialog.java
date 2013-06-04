package com.zedmedia.gravity;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterGroup;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

public class GroupDialog extends Dialog {
	private static final String TAG = "[gravity group]";
	private EditText groupNameText;
	private EditText groupFeeText;
	private GroupInfo groupInfo;
	private Gravity mainActivity;

	public GroupDialog(Gravity gravity) {
		super(gravity);
		mainActivity = gravity;
	}

	@Override
	protected void onStart() {
		super.onStart();
		setContentView(R.layout.add_group);
		setTitle("Add new group");
		groupNameText = (EditText) GroupDialog.this
				.findViewById(R.id.group_name);
		groupFeeText = (EditText) GroupDialog.this.findViewById(R.id.group_fee);
		Button ok = (Button) findViewById(R.id.group_add_button);
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Roster roster = ServerConnection.getInstance().getConnection()
						.getRoster();
				String name = groupNameText.getText().toString();
				double fee = Double.parseDouble(groupFeeText.getText()
						.toString());
				GroupInfo gi = new GroupInfo(name, fee);
				Gson gson = new Gson();
				String groupString = gson.toJson(gi);
				if (groupInfo == null) {
					roster.createGroup(groupString);
				} else {
					Log.d(TAG, "Editing group: " + groupInfo.getJson()
							+ " with new name: " + groupString);
					RosterGroup rg = roster.getGroup(groupInfo.getJson());
					if (rg == null) {
						rg = roster.getGroup(groupInfo.getName());
					}
					if (rg != null) {
						rg.setName(groupString);
						groupInfo.setName(name);
						groupInfo.setFee(fee);
					}
				}
				mainActivity.refreshFeeList();
				dismiss();

			}
		});
	}

	public void setGroupInfo(GroupInfo gi) {
		groupInfo = gi;
		groupNameText.setText(groupInfo.getName());
		groupFeeText.setText("" + groupInfo.getFee());
	}
}
