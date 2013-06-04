package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPException;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BuddyDialog extends Dialog {
	private static final String TAG = "[Gravity user]";
	private Gravity chatClient;
	private Roster roster;
	private EditText userIdText;
	private EditText userNameText;
	private EditText userGroupText;
	private RosterEntry editedEntry;

	public BuddyDialog(Gravity gravity) {
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
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String userId = userIdText.getText().toString().trim();
				// userId = ServerConnection.toSHA1(userId);
				userId += "@" + ServerConnection.HOST;
				if (editedEntry == null) {
					try {
						roster.createEntry(userId, userNameText.getText()
								.toString().trim(),
								new String[] { userGroupText.getText()
										.toString().trim() });
					} catch (XMPPException e) {
						Log.e(TAG,
								"Coud not create roster entry: "
										+ e.getMessage());
					}
				} else {
					editedEntry.setName(userNameText.getText().toString());
					String group = userGroupText.getText().toString().trim();
					RosterGroup oldGroup = null;
					for (RosterGroup rg : editedEntry.getGroups()) {
						oldGroup = rg;
						break;
					}
					if (!group.equals(oldGroup.getName())) {
						try {
							oldGroup.removeEntry(editedEntry);
							RosterGroup newGroup = roster.getGroup(group);
							if (newGroup == null) {
								newGroup = roster.createGroup(group);
							}
							newGroup.addEntry(editedEntry);
						} catch (XMPPException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
				dismiss();

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

	public void setRosterEntry(RosterEntry re) {
		editedEntry = re;
		setTitle("Edit friend");
		// remove the id edit text
		userNameText.setText(editedEntry.getName());
		RosterGroup group = null;
		for (RosterGroup g : editedEntry.getGroups()) {
			group = g;
			break;
		}
		if (group != null) {
			userGroupText.setText(group.getName());
		}
		((LinearLayout) userIdText.getParent()).removeView(userIdText);
	}
}