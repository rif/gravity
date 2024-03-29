package com.zedmedia.gravity.xmpp;

import org.jivesoftware.smack.RosterGroup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GravityRosterGroup {
	private String name;
	private double price;

	public GravityRosterGroup() {
	}

	public GravityRosterGroup(String name, double fee) {
		this.name = name;
		this.price = fee;
	}

	public GravityRosterGroup(RosterGroup rg) {
		this(rg.getName());
	}
	
	public GravityRosterGroup(String json) {
		Gson gson = new Gson();
		try {
			GravityRosterGroup gi = gson.fromJson(json, GravityRosterGroup.class);
			setName(gi.getName());
			setPrice(gi.getPrice());
		} catch (JsonSyntaxException jse) {
			setName(json);
			setPrice(0);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double fee) {
		this.price = fee;
	}

	public String toString() {
		return name + " [" + price + "]";
	}

	public String getJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
