package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class JsonApiConfiguration extends Configuration {
	
	@JsonProperty("mongo")
	private MongoConfig mongo;

	public MongoConfig getMongo() {
		return mongo;
	}

}
