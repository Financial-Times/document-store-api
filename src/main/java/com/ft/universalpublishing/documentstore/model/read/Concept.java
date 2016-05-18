package com.ft.universalpublishing.documentstore.model.read;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

@JsonPropertyOrder({"tmeIdentifier", "prefLabel"})
public class Concept  {

    private String tmeIdentifier;
    private String prefLabel;

    public Concept(@JsonProperty("tmeIdentifier") String tmeIdentifier,
                      @JsonProperty("prefLabel") String prefLabel) {
        this.tmeIdentifier = tmeIdentifier;
        this.prefLabel = prefLabel;
    }

    public String getTmeIdentifier() {
        return tmeIdentifier;
    }

    public void setTmeIdentifier(String tmeIdentifier) {
        this.tmeIdentifier = tmeIdentifier;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Concept that = (Concept) obj;

        return Objects.equals(this.tmeIdentifier, that.tmeIdentifier) && Objects.equals(this.prefLabel, that.prefLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tmeIdentifier, this.prefLabel);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tmeIdentifier", tmeIdentifier)
                .add("prefLabel", prefLabel)
                .toString();

    }
}
