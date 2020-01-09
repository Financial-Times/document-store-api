package com.ft.universalpublishing.documentstore.model.read;

import lombok.Data;

import java.util.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

@Data
public class Context {

    private Map<String, Object> map;

    private List<String> uuids;

    private String collection;

    private Map<String, Object> contentMap;

    private Set<UUID> validatedUuids;

    private UriInfo uriInfo;

    private HttpHeaders httpHeaders;

    public Context() {
        uuids = new ArrayList<>();
        map = new HashMap<>();
    }

    public String getUuid() {
        return uuids.get(0);
    }

    public List<String> getUuids() {
        return uuids;
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
