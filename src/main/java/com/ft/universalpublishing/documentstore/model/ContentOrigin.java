package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"originatingSystem", "originatingIdentifier"})
public class ContentOrigin {

	private String originatingIdentifier;
	private String originatingSystem;

	public String getOriginatingIdentifier() {
		return originatingIdentifier;
	}

	public void setOriginatingIdentifier(String originatingIdentifier) {
		this.originatingIdentifier = originatingIdentifier;
	}

	public String getOriginatingSystem() {
		return originatingSystem;
	}

	public void setOriginatingSystem(String originatingSystem) {
		this.originatingSystem = originatingSystem;
	}
}
