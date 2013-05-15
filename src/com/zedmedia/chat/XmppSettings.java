package com.zedmedia.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//settings get input and then connection is established

public class XmppSettings extends Dialog implements
		android.view.View.OnClickListener {
	private static final String HOST = "ded697.ded.reflected.net";
	private static final String PORT = "5222";
	private ChatClient chatClient;
	private SharedPreferences sharedPref;

	public XmppSettings(ChatClient chatClient) {
		super(chatClient);
		this.chatClient = chatClient;
	}

	protected void onStart() {
		super.onStart();
		setContentView(R.layout.settings);
		setTitle("Connection Settings");
		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		sharedPref = chatClient.getPreferences(Context.MODE_PRIVATE);
		setText(R.id.host, sharedPref.getString("" + R.id.host, HOST));
		setText(R.id.port, sharedPref.getString("" + R.id.port, PORT));
		setText(R.id.service, sharedPref.getString("" + R.id.service, ""));
		setText(R.id.userid, sharedPref.getString("" + R.id.userid, ""));
		setText(R.id.password, sharedPref.getString("" + R.id.password, ""));
	}

	public void onClick(View v) {
		String host = getText(R.id.host);
		String port = getText(R.id.port);
		String service = getText(R.id.service);
		String username = getText(R.id.userid);
		String password = getText(R.id.password);

		// write preferences
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("" + R.id.host, host);
		editor.putString("" + R.id.port, port);
		editor.putString("" + R.id.service, service);
		editor.putString("" + R.id.userid, username);
		editor.putString("" + R.id.password, password);
		editor.commit();
		new CreateConnection().execute(host, port, service, username, password);
		dismiss();
	}

	private String getText(int id) {
		EditText widget = (EditText) this.findViewById(id);
		return widget.getText().toString();
	}

	private void setText(int id, String text) {
		EditText widget = (EditText) this.findViewById(id);
		widget.setText(text);
	}

	class CreateConnection extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... strings) {
			String host = strings[0];
			String port = strings[1];
			String service = strings[2];
			String username = strings[3];
			String password = strings[4];
			ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
					host, Integer.parseInt(port), service);
			XMPPConnection connection = new XMPPConnection(connectionConfig);

			try {
				connection.connect();
			} catch (XMPPException ex) {
				chatClient.setConnection(null);
			}
			try {
				connection.login(username, password);

				// Set status to online / available
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				chatClient.setConnection(connection);
			} catch (XMPPException ex) {
				chatClient.setConnection(null);
			}
			return null;
		}
	}
}