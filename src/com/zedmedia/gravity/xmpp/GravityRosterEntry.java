package com.zedmedia.gravity.xmpp;

import org.jivesoftware.smack.RosterEntry;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GravityRosterEntry {
	private RosterEntry rosterEntry;

	public GravityRosterEntry(RosterEntry rosterEntry) {
		this.rosterEntry = rosterEntry;
	}

	public RosterEntry getRosterEntry() {
		return rosterEntry;
	}

	public double getPrice() {
		NamePricePair pair = NamePricePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NamePricePair(rosterEntry.getName(), 0.0);
			rosterEntry.setName(pair.toJson());
		}
		return pair.price;
	}

	public void setPrice(double price) {
		NamePricePair pair = NamePricePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NamePricePair(rosterEntry.getName(), 0.0);
		}
		pair.price = price;
		rosterEntry.setName(pair.toJson());
	}

	public String getName() {
		NamePricePair pair = NamePricePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NamePricePair(rosterEntry.getName(), 0.0);
			rosterEntry.setName(pair.toJson());
		}
		return pair.name;
	}

	public void setName(String name) {
		NamePricePair pair = NamePricePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NamePricePair(name, 0.0);
		}
		pair.name = name;
		rosterEntry.setName(pair.toJson());
	}

	private static class NamePricePair {
		private String name;
		private double price;
		private static Gson gson = new Gson();

		NamePricePair(String name, double price) {
			this.name = name;
			this.price = price;
		}

		String toJson() {
			return gson.toJson(this);
		}

		static NamePricePair fromJson(String json) {
			try {
				return gson.fromJson(json, NamePricePair.class);
			} catch (JsonSyntaxException jse) {
				return null;
			}
		}
	}
}
