package com.ft.universalpublishing.documentstore.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "title", "uuid", "apiUrl", "items"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentList extends Document {

    private String id;
    private String uuid;
    private String title;
    private String apiUrl;
    private List<ContentItem> items;
    
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public List<ContentItem> getItems() {
        return items;
    }
    
    public void setItems(List<ContentItem> items) {
        this.items = items;
    }
    


    @Override
    public void addIds() {
        setId(IDENTIFIER_TEMPLATE + uuid);
        if (items != null) {
            for (ContentItem item: items) {
                if (item.getUuid() != null) {
                    item.setId(IDENTIFIER_TEMPLATE + item.getUuid());
                }
            }
        }
    }

    @Override
    public void addApiUrls(String apiPath) {
        setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "lists", uuid));
        if (items != null) {
            for (ContentItem item: items) {
                if (item.getUuid() != null) {
                    // for now, assume they're all content
                    item.setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "content", item.getUuid()));
                }
            }
        }
    }

    @Override
    public void removePrivateFields() {
        //set to null so they aren't output, there's probably a cleverer way to do this
        setUuid(null);
        set_id(null);
        if (items != null) {
            for (ContentItem item: items) {
                item.setUuid(null);
            }
        }
    }
    
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .toString();
        //TODO - add the rest
                
    }
    
    @Override
    public boolean equals(Object obj) {
 
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final ContentList other = (ContentList) obj;
        return Objects.equal(this.id, other.id)
            && Objects.equal(this.uuid, other.uuid);
        //TODO - add the rest
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid);
        //TODO - add the rest
    }
    
}
