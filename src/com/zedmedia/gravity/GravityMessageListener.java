package com.zedmedia.gravity;

import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.util.Log;

public class GravityMessageListener implements PacketListener {

	@Override
	public void processPacket(Packet packet) {
		Map<RosterEntry, ChatActivity> activeChats = Gravity.getActiveChats();
		Message message = (Message) packet;
		Object newPrice = message.getProperty("new_price");
		if (newPrice != null) {
			// this is a new price message
			Roster roster = ServerConnection.getInstance().getConnection()
					.getRoster();
			RosterEntry entry = roster.getEntry(StringUtils.parseBareAddress(message.getFrom()));
			Log.d("New price: ", message.getFrom() + ":" + entry);
			if (entry != null) {
				GravityRosterEntry re = new GravityRosterEntry(entry);
				re.setFee((Double) newPrice);
			}
			return;
		}
		if (message.getBody() != null) {
			String from = StringUtils.parseBareAddress(message.getFrom());
			RosterEntry entry = ServerConnection.getInstance().getConnection()
					.getRoster().getEntry(from);
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
				Gravity mainActivity = ServerConnection.getMainActivity();
				Intent intent = new Intent(mainActivity, ChatActivity.class);
				intent.putExtra(ServerConnection.USER_ID, entry.getUser());
				intent.putExtra(ServerConnection.MESSAGE_BODY,
						message.getBody());
				mainActivity.startActivity(intent);
				// the new activity registered itself
				chatActivity = activeChats.get(from);
			}
		}
	}
}
