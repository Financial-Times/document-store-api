package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ComparisonChain;

import java.util.Objects;

@JsonPropertyOrder({"authority", "identifierValue"})
public class Identifier implements Comparable<Identifier> {

    private String authority;
    private String identifierValue;

    public Identifier(@JsonProperty("authority") String authority,
                      @JsonProperty("identifierValue") String identifierValue) {
        this.authority = authority;
        this.identifierValue = identifierValue;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    @Override
    public int compareTo(Identifier that) {
        return ComparisonChain.start()
                .compare(this.authority, that.authority)
                .compare(this.identifierValue, that.identifierValue)
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Identifier that = (Identifier) obj;

        return Objects.equals(this.authority, that.authority) && Objects.equals(this.identifierValue, that.identifierValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.authority, this.identifierValue);
    }
}
