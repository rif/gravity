package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Session;

public class Gravity extends Activity implements RosterListener {
	private static final String TAG = "[GRAVITY]";
	private ArrayList<RosterEntry> buddies = new ArrayList<RosterEntry>();
	private ServerConnection serverConnection;
	private Roster roster;
	private ListView list;
	private GroupDialog groupDialog;
	private BuddyDialog buddyDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roster);
		groupDialog = new GroupDialog(this);
		buddyDialog = new BuddyDialog(this);
		serverConnection = ServerConnection.getInstance();
		serverConnection.init(savedInstanceState, this);
		list = (ListView) this.findViewById(R.id.buddyList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final RosterEntry recipientEntry = (RosterEntry) parent
						.getItemAtPosition(position);
				Intent intent = new Intent(Gravity.this, ChatActivity.class);
				intent.putExtra(ServerConnection.USER_ID,
						recipientEntry.getUser());
				startActivity(intent);
			}

		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final RosterEntry entry = (RosterEntry) parent
						.getItemAtPosition(position);
				buddyDialog.show();
				buddyDialog.setRosterEntry(entry);
				buddyDialog.setId(entry.getUser());
				buddyDialog.setName(entry.getName());
				RosterGroup group = null;
				for (RosterGroup g : entry.getGroups()) {
					group = g;
					break;
				}
				if (group != null) {
					buddyDialog.setGroup(group.getName());
				}				

				return true;
			}
		});

		setListAdapter();
	}

	public void setAddUserGroup(String group) {
		buddyDialog.setGroup(group);
	}

	public void setRoster(Roster r) {
		roster = r;
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		roster.addRosterListener(this);

		// clearAllRosterEntries();
		Log.i(TAG, "Getting roster members: " + roster.getEntries().size());
		Collection<RosterEntry> entries = roster.getEntries();
		buddies.clear();
		for (RosterEntry entry : entries) {
			if (entry.getStatus() == null) {
				String name = entry.getName();
				if (name == null || name.trim().equals("")) {
					name = entry.getUser();
				}
				buddies.add(entry);
			}
		}
		if (buddies.size() > 0) {
			runOnUiThread(new Runnable() {
				public void run() {

					setListAdapter();
				}
			});
		}
	}

	// private void clearAllRosterEntries() {
	// Collection<RosterEntry> entries = roster.getEntries();
	// buddies.clear();
	// for (RosterEntry entry : entries) {
	// try {
	// roster.removeEntry(entry);
	// } catch (XMPPException e) {
	// Log.e(TAG, "Could not remove roster entry: " + e.getMessage());
	// }
	// }
	//
	// }

	private void setListAdapter() {
		ArrayAdapter<RosterEntry> adapter = new ArrayAdapter<RosterEntry>(this,
				R.layout.list, buddies);
		list.setAdapter(adapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(
				serverConnection.getFacebookStatusCallback());
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(
				serverConnection.getFacebookStatusCallback());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gravity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit:
			finish();
			return true;
		case R.id.add_user:
			buddyDialog.show();
			return true;
		case R.id.add_group:
			groupDialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void entriesDeleted(Collection<String> addresses) {
		Log.d(TAG, "entries deleted: " + addresses);
		for (String address : addresses) {
			buddies.remove(address);
		}
		setListAdapter();
	}

	public void entriesUpdated(Collection<String> addresses) {
		Log.d(TAG, "entries updated: " + addresses);
	}

	public void presenceChanged(Presence presence) {
		System.out.println("Presence changed: " + presence.getFrom() + " "
				+ presence);
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		Log.d(TAG, "entries addedd: " + addresses);
		for (String address : addresses) {
			RosterEntry entry = roster.getEntry(address);
			if (entry != null) {
				buddies.add(entry);
			}
		}
		setListAdapter();
	}
}
