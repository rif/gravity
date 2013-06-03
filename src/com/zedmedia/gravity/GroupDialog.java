package com.zedmedia.gravity;

import org.jivesoftware.smack.Roster;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class GroupDialog extends Dialog {
	private Gravity chatActivity;

	public GroupDialog(Gravity gravity) {
		super(gravity);
		chatActivity = gravity;
	}

	protected void onStart() {
		super.onStart();
		setContentView(R.layout.add_group);
		setTitle("Add new group");
		Button ok = (Button) findViewById(R.id.group_add_button);
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Roster roster = ServerConnection.getInstance().getConnection()
						.getRoster();
				EditText groupNameText = (EditText) GroupDialog.this
						.findViewById(R.id.group_name);
				String group = groupNameText.getText().toString();
				roster.createGroup(group);
				chatActivity.setAddUserGroup(group);
				dismiss();

			}
		});
	}
}
