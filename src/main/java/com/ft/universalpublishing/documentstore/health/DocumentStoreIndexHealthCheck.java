package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

public class DocumentStoreIndexHealthCheck extends AdvancedHealthCheck {
  private static final String MESSAGE = "MongoDB indexes may not be up-to-date";

  private final MongoDocumentStoreService service;
  private final HealthcheckParameters healthcheckParameters;

  public DocumentStoreIndexHealthCheck(
      MongoDocumentStoreService service, HealthcheckParameters healthcheckParameters) {
    super(healthcheckParameters.getName());
    this.service = service;
    this.healthcheckParameters = healthcheckParameters;
  }

  @Override
  protected AdvancedResult checkAdvanced() {
    if (service.isIndexed()) {
      return AdvancedResult.healthy("OK");
    }

    return AdvancedResult.error(this, MESSAGE);
  }

  @Override
  protected int severity() {
    return healthcheckParameters.getSeverity();
  }

  @Override
  protected String businessImpact() {
    return healthcheckParameters.getBusinessImpact();
  }

  @Override
  protected String technicalSummary() {
    return healthcheckParameters.getTechnicalSummary();
  }

  @Override
  protected String panicGuideUrl() {
    return healthcheckParameters.getPanicGuideUrl();
  }
}
