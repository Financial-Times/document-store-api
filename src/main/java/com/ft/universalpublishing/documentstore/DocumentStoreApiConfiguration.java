package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class DocumentStoreApiConfiguration extends Configuration {
    
    @JsonProperty("apiPath")
    private String apiPath;
	
	@JsonProperty("mongo")
	private MongoConfig mongo;

	public MongoConfig getMongo() {
		return mongo;
	}
	
	public String getApiPath() {
	    return apiPath;
	}

}
