package com.zedmedia.gravity;

import java.util.ArrayList;

import org.jivesoftware.smack.Roster;
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
	private Roster roster;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		text = (EditText) this.findViewById(R.id.message);
		list = (ListView) this.findViewById(R.id.messageList);
		listAdapter = new ArrayAdapter<String>(this, R.layout.list, messages);
		list.setAdapter(listAdapter);
		serverConnection = ServerConnection.getInstance();
		roster = serverConnection.getConnection().getRoster();
		String address = getIntent().getStringExtra(ServerConnection.USER_ID);
		recipient = serverConnection.getConnection().getRoster()
				.getEntry(address);
		Gravity.addActiveChat(recipient, this);
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
		GravityRosterEntry re = new GravityRosterEntry(recipient);
		String title = re.getName();
		if (title == null || title.trim().equals("")) {
			title = StringUtils.parseName(entry.getUser());
		}
		return title;
	}

	public void displayMessage(Message message) {
		String name = StringUtils.parseName(message.getFrom());
		RosterEntry entry = roster.getEntry(message.getFrom());
		if (entry != null) {
			name = getRecipientName(entry);
		}
		displayMessage(name, message.getBody());

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
		Gravity.removeActiveChat(recipient);
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String message = text.getText().toString();
		text.setText("");
		GravityRosterEntry re = new GravityRosterEntry(recipient);
		
		Message msg = new Message(recipient.getUser(), Message.Type.chat);
		Log.d(TAG, "expecting: " + re.getFee());
		msg.setBody(message);		
		msg.addExtension(new GravityExpectedPriceExtension(re.getFee()));
		serverConnection.getConnection().sendPacket(msg);
		messages.add(serverConnection.getConnection().getAccountManager()
				.getAccountAttribute("name")
				+ ":");
		messages.add(message);
		listAdapter.notifyDataSetChanged();
	}
}
