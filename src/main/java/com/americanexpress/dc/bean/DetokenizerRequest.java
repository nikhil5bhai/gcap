package com.americanexpress.dc.bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DetokenizerRequest {
	private String value;
	private String type;
	private Integer sequence;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public String toString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

}
