package com.zedmedia.gravity.xmpp;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class GravityNewPriceExtension implements PacketExtension {
	public static final String ELEMENT_NAME = "gravity";
	public static final String NAMESPACE = "gravity:new_price";
	private double newPrice;

	public GravityNewPriceExtension() {
	}

	public GravityNewPriceExtension(double newPrice) {
		this.newPrice = newPrice;
	}

	@Override
	public String toXML() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<").append(getElementName()).append(" xmlns=\"")
				.append(getNamespace()).append("\">").append(newPrice)
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
			return new GravityNewPriceExtension();
		}
	}
}