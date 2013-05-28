package com.zedmedia.gravity;

import java.util.ArrayList;

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
	private String recipient;
	private EditText text;
	private ListView list;
	ServerConnection serverConnection;

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
		setListAdapter();
		recipient = getIntent().getStringExtra(ServerConnection.USER_ID);
		if (!recipient.endsWith(ServerConnection.HOST)) {
			recipient += "@" + ServerConnection.HOST;
		}
		serverConnection = ServerConnection.getInstance();
		serverConnection.addActiveChat(recipient, this);
		String from = getIntent().getStringExtra(ServerConnection.MESSAGE_FROM);
		String body = getIntent().getStringExtra(ServerConnection.MESSAGE_BODY);
		Log.d(TAG, "FROM: " + from + " BODY: " + body);
		if (from != null && body != null) {
			displayMessage(from, body);
		}
		// try to establish connection
		// SharedPreferences sharedPref = this
		// .getPreferences(Context.MODE_PRIVATE);
		// String username = sharedPref.getString("" + R.id.userid, "");
		// String password = sharedPref.getString("" + R.id.password, "");
		// if (!username.equals("") && !password.equals("")) {
		// new CreateConnection().execute(username, password);
		// }

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
				setListAdapter();
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

		Message msg = new Message(recipient, Message.Type.chat);
		msg.setBody(message);
		serverConnection.getConnection().sendPacket(msg);
		messages.add(serverConnection.getConnection().getUser() + ":");
		messages.add(message);
		setListAdapter();
	}

	private void setListAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list, messages);
		list.setAdapter(adapter);
	}

}
