package com.zedmedia.gravity;

import com.google.gson.Gson;

public class GroupInfo {
	private String name;
	private double fee;

	public GroupInfo() {
	}

	public GroupInfo(String name, double fee) {
		this.name = name;
		this.fee = fee;
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
