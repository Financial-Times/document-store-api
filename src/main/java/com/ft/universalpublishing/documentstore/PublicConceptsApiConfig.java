package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicConceptsApiConfig {

    @JsonProperty
    String baseURL;

    @JsonProperty
    HealthcheckParameters healthcheckParameters;
}
