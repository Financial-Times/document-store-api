package com.ft.universalpublishing.documentstore.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "title", "uuid", "apiUrl", "publishedDate", "items"})
@JsonDeserialize(builder = ContentList.Builder.class)
public class ContentList {

    protected static final String IDENTIFIER_TEMPLATE = "http://api.ft.com/thing/";
    protected static final String API_URL_TEMPLATE = "http://%s/%s/%s";
    private String _id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private String id;
        private UUID uuid;
        private String apiUrl;
        private String title;
        private List<ListItem> items;
        private Date publishedDate;
        private String layoutHint;
        
        public Builder withId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }
        
        public Builder withApiUrl(String url) {
            this.apiUrl = url;
            return this;
        }
        
        public Builder withItems(List<ListItem> items) {
            this.items = items;
            return this;
        }
        
        public Builder withPublishedDate(Date publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }
        
        public Builder withLayoutHint(String layoutHint) {
            this.layoutHint = layoutHint;
            return this;
        }
        
        public ContentList build() {
            ContentList list = new ContentList(id, uuid, apiUrl, title, items, publishedDate, layoutHint);
            return list;
        }
    }

    private String id;
    private String uuid;
    private String title;
    private String apiUrl;
    private List<ListItem> items;
    private Date publishedDate;
    private String layoutHint;
    
    private ContentList(String id, UUID uuid, String apiUrl, String title, List<ListItem> items,
                        Date publishedDate, String layoutHint) {
        
        setId(id);
        if (uuid != null) {
            setUuid(uuid.toString());
        }
        setApiUrl(apiUrl);
        this.title = title;
        this.items = items;
        this.publishedDate = publishedDate;
        this.layoutHint = layoutHint;
    }
    
    public String getId() {
        return id;
    }
    
    private void setId(String id) {
        this.id = id;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    private void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    private void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public List<ListItem> getItems() {
        return items;
    }
    
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
    public Date getPublishedDate() {
        return publishedDate;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getLayoutHint() {
        return layoutHint;
    }
    
    public void addIds() {
        setId(IDENTIFIER_TEMPLATE + uuid);
        if (items != null) {
            for (ListItem item: items) {
                if (item.getUuid() != null) {
                    item.setId(IDENTIFIER_TEMPLATE + item.getUuid());
                }
            }
        }
    }

    public void addApiUrls(String apiPath) {
        setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "lists", uuid));
        if (items != null) {
            for (ListItem item: items) {
                if (item.getUuid() != null) {
                    // for now, assume they're all content
                    item.setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "content", item.getUuid()));
                }
            }
        }
    }

    public void removePrivateFields() {
        //set to null so they aren't output, there's probably a cleverer way to do this
        setUuid(null);
        set_id(null);
        if (items != null) {
            for (ListItem item: items) {
                item.setUuid(null);
            }
        }
    }
    
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("title", title)
                .add("apiUrl", apiUrl)
                .add("items", items)
                .add("publishedDate", publishedDate)
                .add("layoutHint", layoutHint)
                .toString();

    }
    
    @Override
    public boolean equals(Object obj) {
 
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final ContentList other = (ContentList) obj;
        return Objects.equal(this.id, other.id)
            && Objects.equal(this.uuid, other.uuid)
            && Objects.equal(this.title, other.title)
            && Objects.equal(this.apiUrl, other.apiUrl)
            && Objects.equal(this.items, other.items)
            && Objects.equal(this.publishedDate, other.publishedDate)
            && Objects.equal(this.layoutHint, other.layoutHint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid, title, apiUrl, items, publishedDate, layoutHint);
    }
    
}
