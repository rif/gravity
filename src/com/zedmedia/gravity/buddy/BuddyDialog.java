package com.zedmedia.gravity.buddy;

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
import android.widget.LinearLayout;

import com.zedmedia.gravity.Gravity;
import com.zedmedia.gravity.R;
import com.zedmedia.gravity.ServerConnection;
import com.zedmedia.gravity.xmpp.GravityRosterEntry;
import com.zedmedia.gravity.xmpp.GroupInfo;

public class BuddyDialog extends Dialog {
	private static final String TAG = "[Gravity user]";
	private Gravity chatClient;
	private Roster roster;
	private EditText userIdText;
	private EditText userNameText;
	private EditText userGroupText;
	private GravityRosterEntry editedEntry;

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
				userId = ServerConnection.getUser(userId);
				userId += "@" + ServerConnection.HOST;
				Log.d(TAG, "Adding friend: " + userId);
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
					for (RosterGroup rg : editedEntry.getRosterEntry()
							.getGroups()) {
						oldGroup = rg;
						break;
					}
					GroupInfo gi = null;
					if (oldGroup != null) {
						gi = new GroupInfo(oldGroup);
					}
					if (gi == null || !group.equals(gi.getName())) {
						try {
							if (oldGroup != null) {
								oldGroup.removeEntry(editedEntry
										.getRosterEntry());
							}
							RosterGroup newGroup = roster.getGroup(group);
							if (newGroup == null) {
								newGroup = roster.createGroup(group);
							}
							newGroup.addEntry(editedEntry.getRosterEntry());
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

	public void setRosterEntry(GravityRosterEntry re) {
		editedEntry = re;
		setTitle("Edit friend");
		// remove the id edit text
		userNameText.setText(editedEntry.getName());
		RosterGroup group = null;
		for (RosterGroup g : editedEntry.getRosterEntry().getGroups()) {
			group = g;
			break;
		}
		if (group != null) {
			GroupInfo gi = new GroupInfo(group);
			userGroupText.setText(gi.getName());
		}
		((LinearLayout) userIdText.getParent()).removeView(userIdText);
	}
}