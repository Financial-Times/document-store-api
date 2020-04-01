package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class PublicConcordancesApiConfig {

    @JsonProperty
    String host;

    @JsonProperty
    HealthcheckParameters healthcheckParameters;
}
