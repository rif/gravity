package com.zedmedia.gravity;

import org.jivesoftware.smack.Roster;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddGroupDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Gravity chatActivity;

	public AddGroupDialog(Gravity gravity) {
		super(gravity);		
		chatActivity = gravity;
	}

	protected void onStart() {
		super.onStart();
		setContentView(R.layout.add_group);
		setTitle("Add new group");
		Button ok = (Button) findViewById(R.id.group_add_button);
		ok.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Roster roster = ServerConnection.getInstance().getConnection()
				.getRoster();
		EditText groupNameText = (EditText)this.findViewById(R.id.group_name);
		String group = groupNameText.getText().toString();
		roster.createGroup(group);	
		chatActivity.setAddUserGroup(group);
		this.dismiss();
	}

}
