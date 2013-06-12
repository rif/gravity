package com.zedmedia.gravity;

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

	public double getFee() {
		NameFeePair pair = NameFeePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NameFeePair(rosterEntry.getName(), 0.0);
			rosterEntry.setName(pair.toJson());
		}
		return pair.fee;
	}

	public void setFee(double fee) {
		NameFeePair pair = NameFeePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NameFeePair(rosterEntry.getName(), 0.0);
		}
		pair.fee = fee;
		rosterEntry.setName(pair.toJson());
	}

	public String getName() {
		NameFeePair pair = NameFeePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NameFeePair(rosterEntry.getName(), 0.0);
			rosterEntry.setName(pair.toJson());
		}
		return pair.name;
	}

	public void setName(String name) {
		NameFeePair pair = NameFeePair.fromJson(rosterEntry.getName());
		if (pair == null) {
			pair = new NameFeePair(name, 0.0);
		}
		pair.name = name;
		rosterEntry.setName(pair.toJson());
	}

	private static class NameFeePair {
		private String name;
		private double fee;
		private static Gson gson = new Gson();

		NameFeePair(String name, double fee) {
			this.name = name;
			this.fee = fee;
		}

		String toJson() {
			return gson.toJson(this);
		}

		static NameFeePair fromJson(String json) {
			try {
				return gson.fromJson(json, NameFeePair.class);
			} catch (JsonSyntaxException jse) {
				return null;
			}
		}
	}
}
