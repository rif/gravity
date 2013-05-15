package com.zedmedia.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ChatClient extends Activity {
	private ArrayList<String> messages = new ArrayList<String>();
	private Handler handler = new Handler();
	private EditText recipient;
	private EditText text;
	private ListView list;
	private XmppSettings settings;
	private XMPPConnection connection;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_client);

		/*
		 * StrictMode.ThreadPolicy policy = new
		 * StrictMode.ThreadPolicy.Builder() .permitAll().build();
		 * StrictMode.setThreadPolicy(policy);
		 */

		recipient = (EditText) this.findViewById(R.id.recipient);
		text = (EditText) this.findViewById(R.id.message);
		list = (ListView) this.findViewById(R.id.messageList);
		setListAdapter();

		settings = new XmppSettings(this);

		// try to establish connection
		SharedPreferences sharedPref = this
				.getPreferences(Context.MODE_PRIVATE);
		String host = sharedPref.getString("" + R.id.host, "");
		String port = sharedPref.getString("" + R.id.port, "");
		String service = sharedPref.getString("" + R.id.service, "");
		String username = sharedPref.getString("" + R.id.userid, "");
		String password = sharedPref.getString("" + R.id.password, "");
		if (!host.equals("") && !port.equals("") && !username.equals("")
				&& !password.equals("")) {
			settings.new CreateConnection().execute(host, port, service,
					username, password);
		}
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String to = recipient.getText().toString()
				+ "@ded697.ded.reflected.net";
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
		getMenuInflater().inflate(R.menu.chat_client, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			settings.show();
			return true;
		case R.id.quit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
}
