package com.ft.universalpublishing.documentstore.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "title", "uuid", "apiUrl", "publishedDate", "items"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentList extends Document {

    private String id;
    private String uuid;
    private String title;
    private String apiUrl;
    private List<ContentItem> items;
    private DateTime publishedDate;

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

    public DateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(DateTime publishedDate) {
        this.publishedDate = publishedDate;
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
                .add("title", title)
                .add("apiUrl", apiUrl)
                .add("items", items)
                .add("publishedDate", publishedDate)
                .toString();

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentList)) return false;

        ContentList that = (ContentList) o;

        if (apiUrl != null ? !apiUrl.equals(that.apiUrl) : that.apiUrl != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (items != null ? !items.equals(that.items) : that.items != null) return false;
        if (publishedDate != null ? !publishedDate.equals(that.publishedDate) : that.publishedDate != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (apiUrl != null ? apiUrl.hashCode() : 0);
        result = 31 * result + (items != null ? items.hashCode() : 0);
        result = 31 * result + (publishedDate != null ? publishedDate.hashCode() : 0);
        return result;
    }
}
