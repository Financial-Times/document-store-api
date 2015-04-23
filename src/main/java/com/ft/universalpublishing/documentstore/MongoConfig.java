package com.ft.universalpublishing.documentstore;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.universalpublishing.documentstore.exception.BadConfigurationException;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class MongoConfig {

	public MongoConfig(){}
	
	@JsonProperty
	private List<String> hosts = new ArrayList<>(3);

    @JsonProperty @Deprecated
    private String host;

	@Min(1)
    @Max(65535)
    @JsonProperty
    private int port = 27017;
	
	@NotNull
	@JsonProperty
	private String db;

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
        try {
            for(String mirror : hosts) {
               result.add(new ServerAddress(mirror,getPort()));
            }

            if(!Strings.isNullOrEmpty(this.host) && !hosts.contains(host) ) {
                result.add(new ServerAddress(this.host,getPort()));
            }

            return result;
        } catch (UnknownHostException e) {
            throw new BadConfigurationException(e);
        }
    }
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("hosts", hosts)
                .add("host (legacy)", host)
				.add("port", port)
				.add("db", db)
				.toString();
	}
}
