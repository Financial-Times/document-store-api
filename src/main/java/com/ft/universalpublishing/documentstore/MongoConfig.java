package com.ft.universalpublishing.documentstore;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class MongoConfig {

	public MongoConfig(){}
	
	@NotNull
	@JsonProperty
	private String host;

	@Min(1)
    @Max(65535)
    @JsonProperty
    private int port = 27017;
	
	@NotNull
	@JsonProperty
	private String db;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("host", host)
				.add("port", port)
				.add("db", db)
				.toString();
	}
}
