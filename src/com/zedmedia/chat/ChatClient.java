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
	private XmppSettings settings;
	private EditText recipient;
	private EditText text;
	private ListView list;
	private XMPPConnection connection;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_client);

		recipient = (EditText) this.findViewById(R.id.recipient);
		text = (EditText) this.findViewById(R.id.message);
		list = (ListView) this.findViewById(R.id.messageList);
		setListAdapter();

		// Window for getting settings
		settings = new XmppSettings(this);
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String to = recipient.getText().toString();
        String message = text.getText().toString();		

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
				R.id.messageList, messages);
		list.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat_client, menu);
		return true;
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuInflater
	 * inflater = getMenuInflater(); inflater.inflate(R.menu.menu, menu); return
	 * true; }
	 */

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
