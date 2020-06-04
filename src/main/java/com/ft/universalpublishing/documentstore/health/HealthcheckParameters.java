package com.ft.universalpublishing.documentstore.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

public class HealthcheckParameters {

  private String name;
  private int severity;
  private String businessImpact;
  private String technicalSummary;
  private String panicGuideUrl;

  public HealthcheckParameters(
      @NotNull @JsonProperty("name") String name,
      @NotNull @JsonProperty("severity") int severity,
      @NotNull @JsonProperty("businessImpact") String businessImpact,
      @NotNull @JsonProperty("technicalSummary") String technicalSummary,
      @NotNull @JsonProperty("panicGuideUrl") String panicGuideUrl) {
    this.name = name;
    this.severity = severity;
    this.businessImpact = businessImpact;
    this.technicalSummary = technicalSummary;
    this.panicGuideUrl = panicGuideUrl;
  }

  public String getName() {
    return name;
  }

  public int getSeverity() {
    return severity;
  }

  public String getBusinessImpact() {
    return businessImpact;
  }

  public String getTechnicalSummary() {
    return technicalSummary;
  }

  public String getPanicGuideUrl() {
    return panicGuideUrl;
  }
}
