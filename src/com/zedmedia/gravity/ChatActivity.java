package com.zedmedia.gravity;

import java.util.ArrayList;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
	private static final String TAG = "[GRAVITY Chat]";
	private ArrayList<String> messages = new ArrayList<String>();
	private Handler handler = new Handler();
	private RosterEntry recipient;
	private EditText text;
	private ListView list;
	private ArrayAdapter<String> listAdapter;
	private ServerConnection serverConnection;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		// StrictMode.ThreadPolicy policy = new
		// StrictMode.ThreadPolicy.Builder() .permitAll().build();
		// StrictMode.setThreadPolicy(policy);

		text = (EditText) this.findViewById(R.id.message);
		list = (ListView) this.findViewById(R.id.messageList);
		listAdapter = new ArrayAdapter<String>(this, R.layout.list, messages);
		list.setAdapter(listAdapter);
		serverConnection = ServerConnection.getInstance();
		String address = getIntent().getStringExtra(ServerConnection.USER_ID);
		recipient = serverConnection.getConnection().getRoster()
				.getEntry(address);
		serverConnection.addActiveChat(recipient, this);
		String body = getIntent().getStringExtra(ServerConnection.MESSAGE_BODY);
		if (body != null) {
			displayMessage(getRecipientName(recipient), body);
		}

		setTitle(getRecipientName(null));
	}

	public String getRecipientName(RosterEntry entry) {
		if (entry == null) {
			entry = this.recipient;
		}
		String title = recipient.getName();
		if (title == null || title.trim().equals("")) {
			title = entry.getUser();
		}
		return title;
	}

	public void displayMessage(Message message) {
		displayMessage(StringUtils.parseName(message.getFrom()),
				message.getBody());

	}

	public void displayMessage(String from, String body) {
		messages.add(from + ":");
		messages.add(body);
		handler.post(new Runnable() {
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		serverConnection.removeActiveChat(recipient);
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String message = text.getText().toString();
		text.setText("");

		Message msg = new Message(recipient.getUser(), Message.Type.chat);
		Log.d(TAG, "Recipient: " + recipient);
		msg.setBody(message);
		msg.addExtension(new GravityExtension(1.4));
		serverConnection.getConnection().sendPacket(msg);
		messages.add(serverConnection.getConnection().getAccountManager()
				.getAccountAttribute("name")
				+ ":");
		messages.add(message);
		listAdapter.notifyDataSetChanged();
	}
}
