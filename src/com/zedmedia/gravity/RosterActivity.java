package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Session;

public class RosterActivity extends Activity implements RosterListener {
	public final static String USER_ID = "com.zedmedia.gravity.USER_ID";
	private static final String TAG = "[GRAVITY]";
	private ArrayList<String> buddies = new ArrayList<String>();
	private ServerConnection serverConnection;
	private Roster roster;
	private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roster);
		serverConnection = ServerConnection.getInstance();
		serverConnection.init(savedInstanceState, this);
		list = (ListView) this.findViewById(R.id.buddyList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String destinationUserId = (String) parent
						.getItemAtPosition(position);
				Intent intent = new Intent(RosterActivity.this,
						ChatActivity.class);
				intent.putExtra(USER_ID, destinationUserId);
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
		Presence presence;
		buddies.clear();
		for (RosterEntry entry : entries) {
			presence = roster.getPresence(entry.getUser());
			buddies.add(entry.getUser());
			System.out.println(entry.getUser());
			System.out.println(presence);
		}
		runOnUiThread(new Runnable() {
			public void run() {
				setListAdapter();
			}
		});
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
