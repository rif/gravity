package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class AddUserDialog extends Dialog implements View.OnClickListener {
	private static final String TAG = "[Gravity user]";
	private Gravity chatClient;
	private Roster roster;
	private EditText userIdText;
	private EditText userNameText;
	private EditText userGroupText;

	public AddUserDialog(Gravity gravity) {
		super(gravity);
		this.chatClient = gravity;
	}

	protected void onStart() {
		super.onStart();
		setContentView(R.layout.add_user);
		setTitle("Add new friend");
		userIdText = (EditText) this.findViewById(R.id.user_id);
		userNameText = (EditText) this.findViewById(R.id.user_name);
		userGroupText = (EditText) this.findViewById(R.id.user_group);
		Button ok = (Button) findViewById(R.id.user_add_button);
		ok.setOnClickListener(this);
		Button newGroup = (Button) findViewById(R.id.group_new_button);
		newGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AddGroupDialog(chatClient).show();
			}
		});
		roster = ServerConnection.getInstance().getConnection().getRoster();
		List<String> groups = new ArrayList<String>();
		for (RosterGroup rg : roster.getGroups()) {
			groups.add(rg.getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this.chatClient, android.R.layout.simple_dropdown_item_1line,
				groups);
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.user_group);
		textView.setAdapter(adapter);

	}

	@Override
	public void onClick(View arg0) {
		String userId = userIdText.getText().toString();
		//userId = ServerConnection.toSHA1(userId);
		userId += "@" + ServerConnection.HOST;
		try {
			roster.createEntry(userId, userNameText.getText().toString(),
					new String[] { userGroupText.getText().toString() });
		} catch (XMPPException e) {
			Log.e(TAG, "Coud not create roster entry: " + e.getMessage());
		}
		this.dismiss();
	}

	public void setGroup(String group) {
		userGroupText.setText(group);
	}

}