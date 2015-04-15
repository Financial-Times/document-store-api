package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonPropertyOrder({"id", "uuid"})
public class MainImage {
    
    private String id;
    private String uuid;

    public MainImage(@JsonProperty("id") String id,
            @JsonProperty("uuid") String uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainImage that = (MainImage) o;

        return Objects.equal(this.id,  that.id)
                && Objects.equal(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid);
    }
}
