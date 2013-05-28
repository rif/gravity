package com.zedmedia.gravity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.AccountManager;
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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;

public class ServerConnection {
	public final static String USER_ID = "com.zedmedia.gravity.USER_ID";
	public final static String MESSAGE_FROM = "com.zedmedia.gravity.MESSAGE_FROM";
	public final static String MESSAGE_BODY = "com.zedmedia.gravity.MESSAGE_BODY";
	public static final String HOST = "ded697.ded.reflected.net";
	public static final int PORT = 5222;
	public static final String SERVICE = "";
	public static final String TAG = "[Gravity AUTH]";
	private HttpClient httpclient;
	private XMPPConnection connection;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private Gravity mainActivity;
	private Map<String, ChatActivity> activeChats;
	private static ServerConnection serverConnection;

	private ServerConnection() {
		httpclient = new DefaultHttpClient();
	}

	public static ServerConnection getInstance() {
		if (serverConnection == null) {
			serverConnection = new ServerConnection();
		}
		return serverConnection;
	}

	public void init(Bundle savedInstanceState, Gravity main) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		mainActivity = main;
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(mainActivity, null,
						statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(mainActivity);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(mainActivity)
						.setPermissions(Arrays.asList("email")).setCallback(
								statusCallback));
			}
		}
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(mainActivity)
					.setPermissions(Arrays.asList("email")).setCallback(
							statusCallback));
		} else {
			Session.openActiveSession(mainActivity, true, statusCallback);
		}
		activeChats = new HashMap<String, ChatActivity>();
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
						String from = StringUtils.parseBareAddress(message
								.getFrom());
						ChatActivity chatActivity = activeChats.get(from);
						if (chatActivity != null) {
							chatActivity.displayMessage(message);
						} else {
							// start new chat activity
							Intent intent = new Intent(mainActivity,
									ChatActivity.class);
							intent.putExtra(USER_ID, from);							
							intent.putExtra(MESSAGE_FROM,
									StringUtils.parseName(message.getFrom()));
							intent.putExtra(MESSAGE_BODY, message.getBody());
							mainActivity.startActivity(intent);
							// the new activity registered itself
							chatActivity = activeChats.get(from);
						}
					}
				}
			}, filter);
		}
	}

	public XMPPConnection getConnection() {
		return this.connection;
	}

	public Session.StatusCallback getFacebookStatusCallback() {
		return statusCallback;
	}

	public void addActiveChat(String to, ChatActivity activity) {
		activeChats.put(to, activity);
	}

	public void removeActiveChat(String to) {
		activeChats.remove(to);
	}

	public String getCredit() throws IOException {
		String result = "";
		HttpGet httpGet = new HttpGet(
				"http://dev.seeme.com:6018/credit/api/get_credit/");
		HttpResponse response = httpclient.execute(httpGet);
		// System.out.println(response.getStatusLine());
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);
		return result;
	}

	public void login(String username, String email, String password)
			throws IOException {
		HttpPost httpPost = new HttpPost(
				"http://dev.seeme.com:6018/credit/api/login_user/");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", username));
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("password", getPass(username, email)));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		httpclient.execute(httpPost);

		// System.out.println(response.getStatusLine());
		// HttpEntity entity = response.getEntity();
	}

	public void createTransaction() throws IOException {
		HttpPost httpPost = new HttpPost(
				"http://dev.seeme.com:6018/credit/api/create_transaction/");
		List<NameValuePair> nvps1 = new ArrayList<NameValuePair>();
		nvps1.add(new BasicNameValuePair("amount", "3"));
		nvps1.add(new BasicNameValuePair("description", "from java"));
		nvps1.add(new BasicNameValuePair("transaction_class", "award"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps1));
		httpclient.execute(httpPost);
		// System.out.println(response.getStatusLine());
		// HttpEntity entity = response.getEntity();
	}

	static String toSHA1(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		final byte[] hash = md.digest(convertme);
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	static String getPass(String username, String email) {
		String pSource = username + "gravity" + email;
		return toSHA1(pSource.getBytes()).substring(11, 19);
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
				try {
					getInstance().login(username, email, password);

				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				mainActivity.setRoster(connection.getRoster());
			}
			return null;
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			Log.i(TAG, "Session state: " + state);
			if (session.isOpened()) {
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
										String username = toSHA1(
												email.getBytes()).substring(0,
												25);
										String password = getPass(username,
												email);

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
