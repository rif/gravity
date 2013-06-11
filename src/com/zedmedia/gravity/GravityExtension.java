package com.zedmedia.gravity;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class GravityExtension implements PacketExtension {
	public static final String ELEMENT_NAME = "gravity";
	public static final String NAMESPACE = "gravity:expected_price";
	private double expectedFee;

	public GravityExtension() {
	}

	public GravityExtension(double expectedFee) {
		this.expectedFee = expectedFee;
	}

	@Override
	public String toXML() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace()).append("\">").append(expectedFee)
				.append("</").append(getElementName()).append(">");
		return sb.toString();
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String getElementName() {
		return ELEMENT_NAME;
	}

	public static class Provider implements PacketExtensionProvider {
		public PacketExtension parseExtension(XmlPullParser arg0)
				throws Exception {
			return new GravityExtension();
		}
	}
}