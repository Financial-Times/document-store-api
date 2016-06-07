package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "apiUrl", "prefLabel"})
public class Concept  {

    private URI id;
    private UUID uuid;
    private URI apiUrl;
    private String prefLabel;

    public Concept(@JsonProperty("uuid") UUID uuid,
                   @JsonProperty("prefLabel") String prefLabel) {
        this.uuid = uuid;
        this.prefLabel = prefLabel;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public URI getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(URI apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return Objects.equals(id, concept.id) &&
                Objects.equals(uuid, concept.uuid) &&
                Objects.equals(apiUrl, concept.apiUrl) &&
                Objects.equals(prefLabel, concept.prefLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, apiUrl, prefLabel);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("apiUrl", apiUrl)
                .add("prefLabel", prefLabel)
                .toString();
    }
}
