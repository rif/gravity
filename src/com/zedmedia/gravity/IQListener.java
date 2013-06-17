package com.zedmedia.gravity;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import android.util.Log;

public class IQListener implements PacketListener {
	private static final String TAG = "[gravity IQ listener]";

	@Override
	public void processPacket(Packet pkt) {
		Log.d(TAG, "IQQQQQQQQQQ packet: "+ pkt.toXML());
	}
}
