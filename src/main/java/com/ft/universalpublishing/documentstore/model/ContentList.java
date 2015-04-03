package com.ft.universalpublishing.documentstore.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "requestUrl", "content"})
public class ContentList implements Document {

    private String id;
    private String uuid;
    private String requestUrl;
    private List<ContentItem> content;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public List<ContentItem> getContent() {
        return content;
    }
    
    public void setContent(List<ContentItem> content) {
        this.content = content;
    }
    
}
