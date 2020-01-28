package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.UUID;

@ApiModel
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "apiUrl", "prefLabel"})
@Getter
@Setter
@EqualsAndHashCode
public class Concept {

    private URI id;
    private UUID uuid;
    private URI apiUrl;
    private String prefLabel;

    public Concept(@JsonProperty("uuid") UUID uuid,
                   @JsonProperty("prefLabel") String prefLabel) {
        this.uuid = uuid;
        this.prefLabel = prefLabel;
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
