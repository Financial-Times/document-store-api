package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

public class GenericDocumentStoreHealthCheck extends AdvancedHealthCheck {

  private final HealthcheckService service;
  private final HealthcheckParameters healthcheckParameters;

  public GenericDocumentStoreHealthCheck(
      HealthcheckService service, HealthcheckParameters healthcheckParameters) {
    super(healthcheckParameters.getName());
    this.service = service;
    this.healthcheckParameters = healthcheckParameters;
  }

  @Override
  protected AdvancedResult checkAdvanced() {
    if (service.isHealthcheckOK()) {
      return AdvancedResult.healthy("OK");
    }

    return AdvancedResult.error(this, healthcheckParameters.getTechnicalSummary());
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
