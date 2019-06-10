package com.ft.universalpublishing.documentstore.model.read;

import java.util.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;


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

    public Set<UUID> getValidatedUuids() {
        return validatedUuids;
    }

    public void setValidatedUuids(Set<UUID> validatedUuids) {
        this.validatedUuids = validatedUuids;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public Map<String, Object> getContentMap() {
        return contentMap;
    }

    public void setContentMap(Map<String, Object> contentMap) {
        this.contentMap = contentMap;
    }

    public void addParameter(String key, Object parameter) {
        map.put(key, parameter);
    }

    public Object getParameter(String key) {
        return map.get(key);
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}
