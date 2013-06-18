package com.zedmedia.gravity;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

public class FeeIQProvider implements IQProvider {
	public static final String ELEMENT_NAME="gravity";
	public static final String NAMESPACE="custom:iq:gravity";
	
	@Override
	public IQ parseIQ(XmlPullParser arg0) throws Exception {
		Log.d("XXXXXXXXXXX", arg0.toString());
		return null;
	}

}
