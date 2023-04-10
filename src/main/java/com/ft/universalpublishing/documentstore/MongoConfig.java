package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.dropwizard.util.Duration;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class MongoConfig {

  private static final String TEMPLATE =
      "mongodb://%s:%s@%s/%s?ssl=true&replicaSet=rs0&readpreference=secondaryPreferred&retryWrites=False";

  public MongoConfig() {}

  @NotNull @JsonProperty private String address;
  @NotNull @JsonProperty private String username;
  @NotNull @JsonProperty private String password;

  @NotNull @JsonProperty private String db;

  @JsonProperty private Duration serverSelectorTimeout;

  private Duration idleTimeout;

  public String serverURI() {
    return String.format(TEMPLATE, username, password, address, db);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("address", address)
        .add("username", username)
        .add("db", db)
        .add("idleTimeout", idleTimeout)
        .toString();
  }
}
