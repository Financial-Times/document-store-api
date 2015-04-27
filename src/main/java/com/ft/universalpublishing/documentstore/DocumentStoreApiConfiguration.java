package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;
import io.dropwizard.Configuration;

public class DocumentStoreApiConfiguration extends Configuration {
    
    @JsonProperty("apiPath")
    private String apiPath;
	
	@JsonProperty("mongo")
	private MongoConfig mongo;

    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreApiConfiguration(@JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters) {
        super();
        this.healthcheckParameters = healthcheckParameters;
    }

	public MongoConfig getMongo() {
		return mongo;
	}
	
	public String getApiPath() { return apiPath; }

    public HealthcheckParameters getHealthcheckParameters() { return healthcheckParameters; }

}
