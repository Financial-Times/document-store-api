package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;
import io.dropwizard.Configuration;

public class DocumentStoreApiConfiguration extends Configuration {

    private final String apiHost;
	private final MongoConfig mongo;

    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreApiConfiguration(@JsonProperty("mongo") MongoConfig mongo,
            @JsonProperty("apiHost") String apiHost,
            @JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters) {
        super();
        this.mongo = mongo;
        this.apiHost = apiHost;
        this.healthcheckParameters = healthcheckParameters;
    }

	public MongoConfig getMongo() {
		return mongo;
	}

	public String getApiHost() {
        return apiHost;
    }

    public HealthcheckParameters getHealthcheckParameters() {
        return healthcheckParameters;
    }
}
