package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModel;
import java.net.URI;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "apiUrl", "prefLabel", "originalUUID"})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@EqualsAndHashCode
public class Concept {

  private URI id;
  private UUID uuid;
  private URI apiUrl;
  private String prefLabel;
  private String originalUUID;

  public Concept(@JsonProperty("uuid") UUID uuid, @JsonProperty("prefLabel") String prefLabel) {
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
        .add("originalUUID", originalUUID)
        .toString();
  }

  public String extractConceptUuid() {
    String uuidString = null;
    if (uuid != null) {
      uuidString = uuid.toString();
    } else if (id != null) {
      uuidString = id.getPath().split("/")[2];
    }

    return uuidString;
  }
}
