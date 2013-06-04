package com.zedmedia.gravity;

import org.jivesoftware.smack.RosterGroup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GroupInfo {
	private String name;
	private double fee;

	public GroupInfo() {
	}

	public GroupInfo(String name, double fee) {
		this.name = name;
		this.fee = fee;
	}

	public GroupInfo(RosterGroup rg) {
		this(rg.getName());
	}
	
	public GroupInfo(String json) {
		Gson gson = new Gson();
		try {
			GroupInfo gi = gson.fromJson(json, GroupInfo.class);
			setName(gi.getName());
			setFee(gi.getFee());
		} catch (JsonSyntaxException jse) {
			setName(json);
			setFee(0);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getFee() {
		return fee;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	public String toString() {
		return name + " [" + fee + "]";
	}

	public String getJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
