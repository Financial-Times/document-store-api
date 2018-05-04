package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.mongodb.ServerAddress;
import io.dropwizard.util.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


public class MongoConfig {

  public MongoConfig(){}

  @JsonProperty
  private List<String> addresses = new ArrayList<>();

  @JsonProperty @Deprecated
  private List<String> hosts = new ArrayList<>(3);

  @JsonProperty @Deprecated
  private String host;

  @Min(1)
  @Max(65535)
  @JsonProperty @Deprecated
  private int port = 27017;

  @NotNull
  @JsonProperty
  private String db;

  @JsonProperty
  private Duration serverSelectorTimeout;

  private Duration idleTimeout;

  public List<String> getHosts() {
    return hosts;
  }

  public void setHosts(List<String> hosts) {
    this.hosts = hosts;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDb() {
    return db;
  }

  public void setDb(String db) {
    this.db = db;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public List<ServerAddress> toServerAddresses() {
    List<ServerAddress> result = new ArrayList<>();
    for(String address : addresses) {
      String[] hostAndPort = address.split(":");
      result.add(new ServerAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
    }
    for(String mirror : hosts) {
      result.add(new ServerAddress(mirror,getPort()));
    }

    if(!Strings.isNullOrEmpty(this.host) && !hosts.contains(host) ) {
      result.add(new ServerAddress(this.host,getPort()));
    }

    return result;
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<String> addresses) {
    this.addresses.clear();
    this.addresses.addAll(addresses);
  }

  public void setIdleTimeout(Duration idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public Duration getIdleTimeout() {
    return idleTimeout;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("hosts", hosts)
            .add("host (legacy)", host)
            .add("port", port)
            .add("db", db)
            .add("idleTimeout", idleTimeout)
            .toString();
  }

  public Duration getServerSelectorTimeout() {
    return serverSelectorTimeout;
  }

  public void setServerSelectorTimeout(Duration serverSelectorTimeout) {
    this.serverSelectorTimeout = serverSelectorTimeout;
  }
}
