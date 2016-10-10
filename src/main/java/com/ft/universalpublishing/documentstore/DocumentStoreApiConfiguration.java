package com.ft.universalpublishing.documentstore;

import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import com.ft.platform.dropwizard.ConfigWithGTG;
import com.ft.platform.dropwizard.GTGConfig;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

import static java.util.Collections.unmodifiableMap;

public class DocumentStoreApiConfiguration extends Configuration implements ConfigWithGTG, ConfigWithAppInfo {

  @JsonProperty
  private GTGConfig gtg = new GTGConfig();

  @JsonProperty
  private AppInfo appInfo = new AppInfo();

  private final String apiHost;
  private final MongoConfig mongo;
  private final String cacheTtl;

  private Map<String, String> contentTypeTemplates;
  private HealthcheckParameters healthcheckParameters;

  public DocumentStoreApiConfiguration(
          @JsonProperty("mongo") MongoConfig mongo,
          @JsonProperty("apiHost") String apiHost,
          @JsonProperty("cacheTtl") String cacheTtl,
          @JsonProperty("contentTypeTemplates") final Map<String, String> contentTypeTemplates,
          @JsonProperty("healthcheckParameters") HealthcheckParameters healthcheckParameters
  ) {
    super();
    this.mongo = mongo;
    this.apiHost = apiHost;
    this.cacheTtl = cacheTtl;
    this.contentTypeTemplates = contentTypeTemplates;
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

  @NotNull
  public Map<String, String> getContentTypeTemplates() {
    return unmodifiableMap(contentTypeTemplates);
  }

  @Override
  public AppInfo getAppInfo() {
    return appInfo;
  }
}
