package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@JsonPropertyOrder({ "authority", "identifierValue" })
@Getter
@Setter
@EqualsAndHashCode
public class Identifier {
    String authority;
    String identifierValue;

    public Identifier(@JsonProperty("authority") String authority,
            @JsonProperty("identifierValue") String identifierValue) {
        this.authority = authority;
        this.identifierValue = identifierValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("authority", authority).add("identifierValue", identifierValue)
                .toString();
    }
}
