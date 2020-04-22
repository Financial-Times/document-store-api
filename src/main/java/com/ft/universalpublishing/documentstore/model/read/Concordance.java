package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@JsonPropertyOrder({ "concept", "identifier" })
@Getter
@Setter
@EqualsAndHashCode
public class Concordance {
    // the concepts returned from public-concordances-api do not have the "uuid"
    // and "prefLabel" props - they will be empty
    Concept concept;
    Identifier identifier;

    public Concordance(@JsonProperty("concept") Concept concept, @JsonProperty("identifier") Identifier identifier) {
        this.concept = concept;
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("concept", concept).add("identifier", identifier).toString();
    }
}
