package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import com.ft.platform.dropwizard.ConfigWithGTG;
import com.ft.platform.dropwizard.GTGConfig;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class DocumentStoreApiConfiguration extends Configuration
    implements ConfigWithAppInfo, ConfigWithGTG {
  @JsonProperty private AppInfo appInfo = new AppInfo();
  @JsonProperty private GTGConfig gtgConfig = new GTGConfig();

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  private final String apiHost;
  private final MongoConfig mongo;
  private final String cacheTtl;
  private HealthcheckParameters connectionHealthcheckParameters;
  private HealthcheckParameters indexHealthcheckParameters;

  public DocumentStoreApiConfiguration(
      @JsonProperty("mongodb") MongoConfig mongo,
      @JsonProperty("apiHost") String apiHost,
      @JsonProperty("cacheTtl") String cacheTtl,
      @JsonProperty("connectionHealthcheckParameters")
          HealthcheckParameters connectionHealthcheckParameters,
      @JsonProperty("indexHealthcheckParameters")
          HealthcheckParameters indexHealthcheckParameters) {
    super();
    this.mongo = mongo;
    this.apiHost = apiHost;
    this.cacheTtl = cacheTtl;
    this.connectionHealthcheckParameters = connectionHealthcheckParameters;
    this.indexHealthcheckParameters = indexHealthcheckParameters;
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

  public HealthcheckParameters getConnectionHealthcheckParameters() {
    return connectionHealthcheckParameters;
  }

  public HealthcheckParameters getIndexHealthcheckParameters() {
    return indexHealthcheckParameters;
  }

  @Override
  public AppInfo getAppInfo() {
    return appInfo;
  }

  @Override
  public GTGConfig getGtg() {
    return gtgConfig;
  }
}
