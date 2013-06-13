package com.zedmedia.gravity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;
import android.content.Intent;
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
	public final static String USER_NAME = "com.zedmedia.gravity.USER_NAME";
	public final static String PASS = "com.zedmedia.gravity.PASS";
	public final static String USER_ID = "com.zedmedia.gravity.USER_ID";
	public final static String MESSAGE_FROM = "com.zedmedia.gravity.MESSAGE_FROM";
	public final static String MESSAGE_BODY = "com.zedmedia.gravity.MESSAGE_BODY";
	// public static final String HOST = "ded697.ded.reflected.net";
	public static final String HOST = "t61";
	public static final int PORT = 5222;
	public static final String SERVICE = "";
	public static final String TAG = "[Gravity AUTH]";
	private XMPPConnection connection;
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private Gravity mainActivity;
	private Map<RosterEntry, ChatActivity> activeChats;
	private static ServerConnection serverConnection;

	private ServerConnection() {
		activeChats = new HashMap<RosterEntry, ChatActivity>();
	}

	public static ServerConnection getInstance() {
		if (serverConnection == null) {
			serverConnection = new ServerConnection();
		}
		return serverConnection;
	}

	public void setMainActivity(Gravity gravity) {
		mainActivity = gravity;
	}

	public void initFbLogin(Bundle savedInstanceState, Gravity main) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
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
	}

	// Called by settings when connection is established
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}

	public void setup() {
		if (connection != null) {
			mainActivity.setRoster(connection.getRoster());
			final ProviderManager pm = ProviderManager.getInstance();
			pm.addExtensionProvider(GravityExtension.ELEMENT_NAME,
					GravityExtension.NAMESPACE, new GravityExtension.Provider());
			// Packet listener to get messages sent to logged in user
			connection.addPacketListener(new IQListener(),
					new PacketTypeFilter(IQ.class));
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String from = StringUtils.parseBareAddress(message
								.getFrom());
						RosterEntry entry = getConnection().getRoster()
								.getEntry(from);
						ChatActivity chatActivity = null;
						if (entry != null) {
							chatActivity = activeChats.get(entry);
						} else {
							// receive messages from users not in your list
							chatActivity = activeChats.get(StringUtils
									.parseBareAddress(from));
						}
						if (chatActivity != null) {
							chatActivity.displayMessage(message);
						} else {
							// start new chat activity
							Intent intent = new Intent(mainActivity,
									ChatActivity.class);
							intent.putExtra(USER_ID, entry.getUser());
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

	public void addActiveChat(RosterEntry to, ChatActivity activity) {
		activeChats.put(to, activity);
	}

	public void removeActiveChat(RosterEntry to) {
		activeChats.remove(to);
	}

	static String toSHA1(String convertme) {
		return ServerConnection.toSHA1(convertme.getBytes());
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

	public static String getUser(String emailOrNumber) {
		return toSHA1(emailOrNumber).substring(0, 25);
	}

	public static String getPass(String username, String email) {
		String pSource = username + "gravity" + email;
		return toSHA1(pSource).substring(11, 19);
	}

	public void login(String username, String password) throws XMPPException {
		XMPPConnection conn = getConnection();
		if (conn != null && conn.isConnected() && !conn.isAuthenticated()) {
			Log.d(TAG, "Logging in: " + username + " : " + password);
			connection.login(username, password);

			// Set status to online / available
			Presence presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
			saveCredentials(username, password);
			setup();
			Log.i(TAG, "User login successful: " + username);
		}
	}

	public void register(String username, String first, String last,
			String email, String password) throws XMPPException {
		XMPPConnection conn = getConnection();
		if (conn != null && conn.isConnected() && !conn.isAuthenticated()) {
			AccountManager accountManager = new AccountManager(connection);
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("name", first + " " + last);
			attributes.put("first", first);
			attributes.put("last", last);
			attributes.put("email", email);
			accountManager.createAccount(username, password, attributes);
			accountManager.getAccountAttributes();
		}
	}

	public void makeConnetion() throws XMPPException {
		XMPPConnection conn = getConnection();
		if (conn == null || !conn.isConnected()) {
			ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
					HOST, PORT, SERVICE);
			XMPPConnection connection = new XMPPConnection(connectionConfig);
			connection.connect();
			setConnection(connection);
		}
	}

	public boolean loginOrRegister(String email, String username, String first,
			String last, String password) {
		XMPPConnection conn = getConnection();
		if (conn != null && conn.isConnected() && !conn.isAuthenticated()) {
			try {
				login(username, password);
			} catch (XMPPException ex1) {
				Log.e(TAG, ex1.getMessage());
				try {
					register(username, first, last, email, password);
					login(username, password);
				} catch (XMPPException ex2) {
					Log.e(TAG, ex2.getMessage());
					setConnection(null);
					return false;
				}
			}
		}
		return true;
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
										String username = getUser(email);
										String password = getPass(username,
												email);

										loginOrRegister(email, username,
												user.getFirstName(),
												user.getLastName(), password);
									}
								}
							}
						});
			}
		}
	}

	private void saveCredentials(String username, String password) {
		mainActivity.getPreferences(Context.MODE_PRIVATE).edit()
				.putString(USER_NAME, username).putString(PASS, password)
				.commit();
	}

	public void logout() {
		getConnection().disconnect();
		setConnection(null);
		Session session = Session.getActiveSession();
		if (session != null && !session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
		saveCredentials("", "");
	}
}
