package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "apiUrl", "webUrl"})
@EqualsAndHashCode
@Getter
@Setter
public class ListItem {
    private String id;
    private String uuid;
    private String apiUrl;
    private String webUrl;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("apiUrl", apiUrl)
                .add("webUrl", webUrl)
                .toString();

    }
}
