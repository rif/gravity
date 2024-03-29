package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.facebook.Session;
import com.zedmedia.gravity.auth.LoginActivity;
import com.zedmedia.gravity.buddy.BuddyDialog;
import com.zedmedia.gravity.buddy.BuddyListAdapter;
import com.zedmedia.gravity.credit.CreditActivity;
import com.zedmedia.gravity.group.GroupDialog;
import com.zedmedia.gravity.group.GroupPriceDialog;
import com.zedmedia.gravity.xmpp.GravityRosterEntry;

public class Gravity extends Activity implements RosterListener {
	private static final String TAG = "[GRAVITY]";
	private ArrayList<GravityRosterEntry> buddies = new ArrayList<GravityRosterEntry>();
	private static Map<RosterEntry, ChatActivity> activeChats;
	private BuddyListAdapter listAdapter;
	private ServerConnection serverConnection;
	private Roster roster;
	private ListView list;
	private GroupDialog groupDialog;
	private BuddyDialog buddyDialog;
	private GroupPriceDialog feesDialog;

	@Override
	protected void onResume() {
		super.onResume();
		connect();
		refreshRoster();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roster);
		groupDialog = new GroupDialog(this);
		buddyDialog = new BuddyDialog(this);
		feesDialog = new GroupPriceDialog(this);
		listAdapter = new BuddyListAdapter(this, R.layout.list, buddies);
		serverConnection = ServerConnection.getInstance();
		ServerConnection.setMainActivity(this);
		activeChats = new HashMap<RosterEntry, ChatActivity>();
		list = (ListView) this.findViewById(R.id.buddyList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final GravityRosterEntry recipientEntry = (GravityRosterEntry) parent
						.getItemAtPosition(position);
				Intent intent = new Intent(Gravity.this, ChatActivity.class);
				intent.putExtra(ServerConnection.USER_ID, recipientEntry
						.getRosterEntry().getUser());
				startActivity(intent);
			}

		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final GravityRosterEntry entry = (GravityRosterEntry) parent
						.getItemAtPosition(position);
				buddyDialog.show();
				buddyDialog.setRosterEntry(entry);
				return true;
			}
		});
		list.setAdapter(listAdapter);
	}

	private void connect() {
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		String username = sharedPref.getString(ServerConnection.USER_NAME, "");
		String password = sharedPref.getString(ServerConnection.PASS, "");
		new XMPPConnect().execute(username, password);
	}

	public void setRoster(Roster r) {
		roster = r;
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		roster.addRosterListener(this);

		// clearAllRosterEntries();
		refreshRoster();
	}

	public static Map<RosterEntry, ChatActivity> getActiveChats() {
		return activeChats;
	}

	public static void addActiveChat(RosterEntry to, ChatActivity activity) {
		activeChats.put(to, activity);
	}

	public static void removeActiveChat(RosterEntry to) {
		activeChats.remove(to);
	}

	private void refreshRoster() {
		if (roster == null) {
			return;
		}
		Collection<RosterEntry> entries = roster.getEntries();
		buddies.clear();
		for (RosterEntry entry : entries) {

			if (entry.getStatus() == null) {
				String name = entry.getName();
				if (name == null || name.trim().equals("")) {
					name = entry.getUser();
				}
				buddies.add(new GravityRosterEntry(entry));
			}
		}
		if (buddies.size() > 0) {
			runOnUiThread(new Runnable() {
				public void run() {
					listAdapter.notifyDataSetChanged();
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

	@Override
	public void onStart() {
		super.onStart();
		Session s = Session.getActiveSession();
		if (s != null) {
			s.addCallback(serverConnection.getFacebookStatusCallback());
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Session s = Session.getActiveSession();
		if (s != null) {
			s.removeCallback(serverConnection.getFacebookStatusCallback());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session s = Session.getActiveSession();
		if (s != null) {
			s.onActivityResult(this, requestCode, resultCode, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		if (session != null) {
			Session.saveSession(session, outState);
		}
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
		case R.id.logout:
			ServerConnection.getInstance().logout();
			startActivity(new Intent(Gravity.this, LoginActivity.class));
			return true;
		case R.id.add_user:
			buddyDialog.show();
			return true;
		case R.id.credit:
			Intent intent = new Intent(this, CreditActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.add_group:
			groupDialog.show();
			return true;
		case R.id.edit_fees:
			feesDialog.show();
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
		listAdapter.notifyDataSetChanged();
	}

	public void entriesUpdated(Collection<String> addresses) {
		Log.d(TAG, "entries updated: " + addresses);
		refreshRoster();
	}

	public void presenceChanged(Presence presence) {
		System.out.println("Presence changed: " + presence.getFrom() + " "
				+ presence);
		refreshRoster();
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		Log.d(TAG, "entries addedd: " + addresses);
		for (String address : addresses) {
			RosterEntry entry = roster.getEntry(address);
			if (entry != null) {
				buddies.add(new GravityRosterEntry(entry));
			}
		}
		listAdapter.notifyDataSetChanged();
	}

	public void refreshFeeList() {
		feesDialog.refreshList();
	}

	public class XMPPConnect extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			String username = params[0];
			String password = params[1];
			try {
				serverConnection.makeConnetion();
			} catch (XMPPException e) {
				return false;
			}
			if (!username.equals("") && !password.equals("")) {
				try {
					serverConnection.login(username, password);
				} catch (Exception e) {
					return false;
				}
			} else {
				startActivity(new Intent(Gravity.this, LoginActivity.class));
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			// TODO: check success
		}

		@Override
		protected void onCancelled() {
			// TODO: stop loading
		}
	}
}
