package com.ft.universalpublishing.documentstore.model.read;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@JsonPropertyOrder({ "concordances" })
@Getter
@Setter
@EqualsAndHashCode
public class Concordances {
    List<Concordance> concordances;

    public Concordances(@JsonProperty("concordances") List<Concordance> concordances) {
        this.concordances = concordances;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("concordances", concordances).toString();
    }
}
