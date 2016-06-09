package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "apiUrl", "webUrl"})
public class ListItem {
    private String id;
    private String uuid;
    private String apiUrl;
    private String webUrl;
    
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

    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }
    
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("apiUrl", apiUrl)
                .add("webUrl", webUrl)
                .toString();
                
    }
    
    @Override
    public boolean equals(Object obj) {
 
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final ListItem other = (ListItem) obj;
        return Objects.equal(this.id, other.id)
            && Objects.equal(this.uuid, other.uuid)
            && Objects.equal(this.apiUrl, other.apiUrl)
            && Objects.equal(this.webUrl, other.webUrl);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid, apiUrl, webUrl);
    }
}
