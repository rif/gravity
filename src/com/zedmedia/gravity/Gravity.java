package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;

public class Gravity extends Activity implements RosterListener {	
	private static final String TAG = "[GRAVITY]";
	private ArrayList<String> buddies = new ArrayList<String>();
	private ServerConnection serverConnection;
	private Roster roster;
	private ListView list;
	private TextView addBuddyText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roster);
		serverConnection = ServerConnection.getInstance();
		serverConnection.init(savedInstanceState, this);
		addBuddyText = (TextView) this.findViewById(R.id.addBuddyText);
		list = (ListView) this.findViewById(R.id.buddyList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String destinationUserId = (String) parent
						.getItemAtPosition(position);				
				Intent intent = new Intent(Gravity.this,
						ChatActivity.class);				
				intent.putExtra(ServerConnection.USER_ID, destinationUserId);				
				startActivity(intent);
			}

		});
		setListAdapter();
	}

	public void setRoster(Roster r) {
		roster = r;
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		roster.addRosterListener(this);

		Log.i(TAG, "Getting roster members: " + roster.getEntries().size());
		Collection<RosterEntry> entries = roster.getEntries();
		buddies.clear();
		for (RosterEntry entry : entries) {
			buddies.add(entry.getUser());			
		}
		runOnUiThread(new Runnable() {
			public void run() {
				setListAdapter();
			}
		});
	}

	/*private void clearAllRosterEntries() {
		Collection<RosterEntry> entries = roster.getEntries();
		buddies.clear();
		for (RosterEntry entry : entries) {
			try {
				roster.removeEntry(entry);
			} catch (XMPPException e) {
				Log.e(TAG, "Could not remove roster entry: " + e.getMessage());
			}
		}
	}*/

	/** Called when the user clicks the Add Buddy button */
	public void addBuddy(View view) {
		String userId = addBuddyText.getText().toString();
		addBuddyText.setText("");

		try {
			Log.d(TAG, "Roster: " + roster);
			roster.createEntry(userId, userId, null);
		} catch (XMPPException e) {
			Log.e(TAG, "Coud not create roster entry: " + e.getMessage());
		}
	}

	private void setListAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void entriesDeleted(Collection<String> addresses) {
		Log.d(TAG, "entries deleted: " + addresses);
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

	}

}
