package com.ft.universalpublishing.documentstore.model.read;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

@JsonPropertyOrder({"id", "prefLabel"})
public class Type  {

    private String id;
    private String prefLabel;

    public Type(@JsonProperty("id") String id,
                      @JsonProperty("prefLabel") String prefLabel) {
        this.id = id;
        this.prefLabel = prefLabel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

        Type that = (Type) obj;

        return Objects.equals(this.id, that.id) && Objects.equals(this.prefLabel, that.prefLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.prefLabel);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("prefLabel", prefLabel)
                .toString();

    }
}
