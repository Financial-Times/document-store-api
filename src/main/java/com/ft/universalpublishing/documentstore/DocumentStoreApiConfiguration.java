package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class DocumentStoreApiConfiguration extends Configuration {

    private final String apiHost;
	private final MongoConfig mongo;

    private Map<String, String> contentTypeTemplates;
    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreApiConfiguration(@JsonProperty("mongo") MongoConfig mongo,
            @JsonProperty("apiHost") String apiHost,
            @JsonProperty("contentTypeTemplates") final Map<String, String> contentTypeTemplates,
            @JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters) {
        super();
        this.mongo = mongo;
        this.apiHost = apiHost;
        this.contentTypeTemplates = contentTypeTemplates;
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

    @NotNull
    public Map<String, String> getContentTypeTemplates() {
        return unmodifiableMap(contentTypeTemplates);
    }
}
