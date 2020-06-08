package com.ft.universalpublishing.documentstore.model.read;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import lombok.Data;
import org.bson.Document;

@Data
public class Context {
  private Map<String, Object> map;
  private List<String> uuids;
  private String collection;
  private Map<String, Object> contentMap;
  private Set<UUID> validatedUuids;
  private UriInfo uriInfo;
  private HttpHeaders httpHeaders;
  private String conceptUUID;
  private String listType;
  private String searchTerm;
  private String webUrl;
  private String standfirst;
  private List<Document> documents;

  public Context() {
    uuids = new ArrayList<>();
    map = new HashMap<>();
  }

  public String getUuid() {
    return uuids.get(0);
  }

  public void setUuids(List<String> uuids) {
    this.uuids.addAll(uuids);
  }

  public void setUuids(String... uuids) {
    Collections.addAll(this.uuids, uuids);
  }

  public void addParameter(String key, Object parameter) {
    map.put(key, parameter);
  }

  public Object getParameter(String key) {
    return map.get(key);
  }
}
