package com.zedmedia.gravity;

import java.util.ArrayList;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
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
		recipient = getIntent().getStringExtra(RosterActivity.USER_ID);
		serverConnection = ServerConnection.getInstance();
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
		String fromName = StringUtils.parseName(message.getFrom());
		messages.add(fromName + ":");
		messages.add(message.getBody());
		handler.post(new Runnable() {
			public void run() {
				setListAdapter();
			}
		});
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		String to = recipient;// + "@" + ServerConnection.HOST;
		String message = text.getText().toString();
		text.setText("");

		Message msg = new Message(to, Message.Type.chat);
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
