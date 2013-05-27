package com.zedmedia.gravity;

import java.util.ArrayList;
import java.util.Arrays;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class Gravity extends Activity {
	private static final String HOST = "ded697.ded.reflected.net";
	private static final int PORT = 5222;
	private static final String SERVICE = "";
	private ArrayList<String> messages = new ArrayList<String>();
	private Handler handler = new Handler();
	private EditText recipient;
	private EditText text;
	private ListView list;
	private XMPPConnection connection;

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
		//SharedPreferences sharedPref = this
		//		.getPreferences(Context.MODE_PRIVATE);
		//String username = sharedPref.getString("" + R.id.userid, "");
		//String password = sharedPref.getString("" + R.id.password, "");
		//if (!username.equals("") && !password.equals("")) {
		//	new CreateConnection().execute(username, password);
		//}
		// new AsyncTask<Void, Void, Void>() {
		// private String credit = "";
		// @Override
		// protected Void doInBackground(Void... params) {
		// try {
		// WebService.getInstance().login();
		// credit = WebService.getInstance().getCredit();
		// messages.add("Credit: " + credit);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// runOnUiThread(new Runnable() {
		// public void run() {
		// setListAdapter();
		// }
		// });
		//
		// return null;
		// }
		// }.execute();
		// Intent intent = new Intent(this, AccountManagerActivity.class);
		// startActivity(intent);
		LoginButton authButton = (LoginButton) findViewById(R.id.authButton);		
		authButton.setReadPermissions(Arrays.asList("email"));
		//fbLogin();

	}

	public void fbLogin() {
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {

			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				if (session.isOpened()) {
					//session.requestNewPublishPermissions(Session.NewPermissionsRequest(this, Arrays.asList("email")));
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
										String username = user.getFirstName() + user.getMiddleName() + user.getLastName();
										String password = "";
										String email = (String) response.getGraphObject().getProperty("email");
										//user.asMap().get("email")
										System.out.println("UUUU: " + email);										
										new CreateConnection().execute(username, password);
									}
								}
							});
				}
			}
		});
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
						String fromName = StringUtils.parseBareAddress(message
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	class CreateConnection extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strings) {
			String username = strings[0];
			String password = strings[1];
			ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
					HOST, PORT, SERVICE);
			XMPPConnection connection = new XMPPConnection(connectionConfig);

			try {
				connection.connect();
			} catch (XMPPException ex) {
				setConnection(null);
			}
			try {
				connection.login(username, password);

				// Set status to online / available
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				setConnection(connection);
			} catch (XMPPException ex) {
				setConnection(null);
			}
			return null;
		}
	}
}
