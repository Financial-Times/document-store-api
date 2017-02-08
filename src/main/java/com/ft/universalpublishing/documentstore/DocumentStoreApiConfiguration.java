package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import com.ft.platform.dropwizard.ConfigWithGTG;
import com.ft.platform.dropwizard.GTGConfig;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;

import io.dropwizard.Configuration;

public class DocumentStoreApiConfiguration extends Configuration implements ConfigWithGTG, ConfigWithAppInfo {

    private final String apiHost;
    private final MongoConfig mongo;
    private final String cacheTtl;
    @JsonProperty
    private GTGConfig gtg = new GTGConfig();
    @JsonProperty
    private AppInfo appInfo = new AppInfo();
    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreApiConfiguration(
            @JsonProperty("mongo") MongoConfig mongo,
            @JsonProperty("apiHost") String apiHost,
            @JsonProperty("cacheTtl") String cacheTtl,
            @JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters
    ) {
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

    @Override
    public GTGConfig getGtg() {
        return gtg;
    }

    public HealthcheckParameters getHealthcheckParameters() {
        return healthcheckParameters;
    }

    @Override
    public AppInfo getAppInfo() {
        return appInfo;
    }
}
