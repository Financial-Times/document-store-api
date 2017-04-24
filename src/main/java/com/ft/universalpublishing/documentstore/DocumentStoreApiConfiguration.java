package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;

import io.dropwizard.Configuration;

public class DocumentStoreApiConfiguration extends Configuration {

    private final String apiHost;
    private final MongoConfig mongo;
    private final String cacheTtl;

    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreApiConfiguration(
            @JsonProperty("mongo") MongoConfig mongo,
            @JsonProperty("apiHost") String apiHost,
            @JsonProperty("cacheTtl") String cacheTtl,
            @JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters) {
        super();
        this.mongo = mongo;
        this.apiHost = apiHost;
        this.cacheTtl = cacheTtl;
        this.healthcheckParameters = healthcheckParameters;
    }

    public MongoConfig getMongo() {
        return mongo;
    }

    public String getApiHost() {
        return apiHost;
    }

    public String getCacheTtl() {
        return cacheTtl;
    }

    public HealthcheckParameters getHealthcheckParameters() {
        return healthcheckParameters;
    }

}
