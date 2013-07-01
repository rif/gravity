package com.zedmedia.gravity.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import android.util.Log;

public class IQListener implements PacketListener {
	@Override
	public void processPacket(Packet pkt) {
		Log.d("IQ: ", pkt.toXML());
	}
}
