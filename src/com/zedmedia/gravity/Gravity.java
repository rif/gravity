package com.zedmedia.gravity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class Gravity extends Activity {
	private static final String HOST = "ded697.ded.reflected.net";
	private static final int PORT = 5222;
	private static final String SERVICE = "";
	private static final String TAG = "GRAVITY";
	private ArrayList<String> messages = new ArrayList<String>();
	private Handler handler = new Handler();
	private EditText recipient;
	private EditText text;
	private ListView list;
	private XMPPConnection connection;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gravity);

		// StrictMode.ThreadPolicy policy = new
		// StrictMode.ThreadPolicy.Builder() .permitAll().build();
		// StrictMode.setThreadPolicy(policy);

		recipient = (EditText) this.findViewById(R.id.recipient);
		text = (EditText) this.findViewById(R.id.message);
		list = (ListView) this.findViewById(R.id.messageList);
		setListAdapter();
		// try to establish connection
		// SharedPreferences sharedPref = this
		// .getPreferences(Context.MODE_PRIVATE);
		// String username = sharedPref.getString("" + R.id.userid, "");
		// String password = sharedPref.getString("" + R.id.password, "");
		// if (!username.equals("") && !password.equals("")) {
		// new CreateConnection().execute(username, password);
		// }
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setPermissions(Arrays.asList("email")).setCallback(
								statusCallback));
			}
		}
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setPermissions(
					Arrays.asList("email")).setCallback(statusCallback));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
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

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String to = recipient.getText().toString() + "@" + HOST;
		String message = text.getText().toString();
		text.setText("");

		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(message);
		connection.sendPacket(msg);
		messages.add(connection.getUser() + ":");
		messages.add(message);
		setListAdapter();
	}

	// Called by settings when connection is established
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
		if (connection != null) {
			// Packet listener to get messages sent to logged in user
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseName(message
								.getFrom());
						messages.add(fromName + ":");
						messages.add(message.getBody());
						handler.post(new Runnable() {
							public void run() {
								setListAdapter();
							}
						});
					}
				}
			}, filter);
		}
	}

	public XMPPConnection getConnection() {
		return this.connection;
	}

	private void setListAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list, messages);
		list.setAdapter(adapter);
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

	class CreateConnection extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strings) {
			String email = strings[0];
			String username = strings[1];
			String first = strings[2];
			String last = strings[3];
			String password = strings[4];
			ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
					HOST, PORT, SERVICE);
			XMPPConnection connection = new XMPPConnection(connectionConfig);

			try {
				connection.connect();
			} catch (XMPPException ex) {
				setConnection(null);
			}
			try {
				Log.d(TAG, "Logging in: " + username + " : " + password);
				connection.login(username, password);

				// Set status to online / available
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				setConnection(connection);
				Log.i(TAG, "User login successful: " + username);
			} catch (XMPPException ex1) {
				Log.e(TAG, "Login failed: " + ex1.getMessage());
				// create the account if does not exists
				Log.i(TAG, "User does not exists creating: " + username);
				AccountManager accountManager = new AccountManager(connection);
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put("name", first + " " + last);
				attributes.put("first", first);
				attributes.put("last", last);
				attributes.put("email", email);
				try {
					accountManager
							.createAccount(username, password, attributes);
					accountManager.getAccountAttributes();
					connection.login(username, password);
					// Set status to online / available
					Presence presence = new Presence(Presence.Type.available);
					connection.sendPacket(presence);
					setConnection(connection);
					Log.i(TAG, "User login successful after user creation: "
							+ username);
				} catch (XMPPException ex2) {
					Log.e(TAG, "Login failed: " + ex2.getMessage());
					setConnection(null);
				}
			}
			if (getConnection() != null) {
				Roster roster = connection.getRoster();
				roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
				try {
					roster.createEntry("rif", "rifus", null);
				} catch (XMPPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				roster.addRosterListener(new RosterListener() {
					public void entriesDeleted(Collection<String> addresses) {
						Log.d(TAG, "entries deleted: " + addresses);
					}

					public void entriesUpdated(Collection<String> addresses) {
						Log.d(TAG, "entries updated: " + addresses);
					}

					public void presenceChanged(Presence presence) {
						System.out.println("Presence changed: "
								+ presence.getFrom() + " " + presence);
					}

					@Override
					public void entriesAdded(Collection<String> addresses) {
						Log.d(TAG, "entries addedd: " + addresses);
						
					}
				});
				Log.i(TAG, "Getting roster members: "
						+ roster.getEntries().size());
				Collection<RosterEntry> entries = roster.getEntries();
				Presence presence;
				for (RosterEntry entry : entries) {					
			            presence = roster.getPresence(entry.getUser());

			            System.out.println(entry.getUser());
			            System.out.println(presence.getType().name());
			            //System.out.println(presence.getStatus());			        
				}
				try {
					WebService.getInstance().login(username, email, password);
					String credit = WebService.getInstance().getCredit();
					messages.add("Credit: " + credit);
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}

				runOnUiThread(new Runnable() {
					public void run() {
						setListAdapter();
					}
				});
			}
			return null;
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.i(TAG, "open for reading");
				// make request to the /me API
				Request.executeMeRequestAsync(session,
						new Request.GraphUserCallback() {

							// callback after Graph API response with user
							// object
							@Override
							public void onCompleted(GraphUser user,
									Response response) {
								if (user != null) {
									// connect
									String email = (String) response
											.getGraphObject().getProperty(
													"email");
									if (email != null) {
										String username = WebService.toSHA1(
												email.getBytes()).substring(0,
												25);
										String password = WebService.getPass(
												username, email);

										new CreateConnection().execute(email,
												username, user.getFirstName(),
												user.getLastName(), password);
									}
								}
							}
						});
			}
		}

	}

}
