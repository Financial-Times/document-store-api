package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "title", "uuid", "apiUrl", "concept", "listType", "items", "layoutHint", "publishReference", "lastModified"})
@JsonDeserialize(builder = ContentList.Builder.class)
public class ContentList {

    protected static final String IDENTIFIER_TEMPLATE = "http://api.ft.com/things/";
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
        private Concept concept;
        private String listType;
        private String title;
        private List<ListItem> items;
        private String layoutHint;
        private String publishReference;
        private Date lastModified;

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
        
        public Builder withConcept(Concept concept) {
            this.concept = concept;
            return this;
        }
        
        public Builder withListType(String listType) {
            this.listType = listType;
            return this;
        }

        public Builder withItems(List<ListItem> items) {
            this.items = items;
            return this;
        }

        public Builder withLayoutHint(String layoutHint) {
            this.layoutHint = layoutHint;
            return this;
        }

        public Builder withPublishReference(String publishReference) {
            this.publishReference = publishReference;
            return this;
        }

        public Builder withLastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public ContentList build() {
            ContentList list = new ContentList(id, uuid, apiUrl, concept, listType, title, items, layoutHint, publishReference, lastModified);
            return list;
        }
    }

    private String id;
    private String uuid;
    private String title;
    private String apiUrl;
    private Concept concept;
    private String listType;
    private List<ListItem> items;
    private Date publishedDate;
    private String layoutHint;
    private String publishReference;
    private Date lastModified;

    private ContentList(String id, UUID uuid, String apiUrl, Concept concept, String listType, String title, List<ListItem> items,
                        String layoutHint, String publishReference, Date lastModified) {

        setId(id);
        if (uuid != null) {
            setUuid(uuid.toString());
        }
        setApiUrl(apiUrl);
        this.concept = concept;
        this.listType = listType;
        this.title = title;
        this.items = items;
        this.layoutHint = layoutHint;
        this.publishReference = publishReference;
        this.lastModified = lastModified;
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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public List<ListItem> getItems() {
        return items;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getPublishedDate() {
        return publishedDate;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getLayoutHint() {
        return layoutHint;
    }

    public String getPublishReference() {
        return publishReference;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getLastModified() {
        return lastModified;
    }

    public void addIds() {
        setId(IDENTIFIER_TEMPLATE + uuid);
        if (items != null) {
            for (ListItem item : items) {
                if (item.getUuid() != null) {
                    item.setId(IDENTIFIER_TEMPLATE + item.getUuid());
                }
            }
        }
        if(concept != null){
            concept.setId(URI.create(IDENTIFIER_TEMPLATE + concept.getUuid().toString()));
        }
    }

    public void addApiUrls(String apiPath) {
        setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "lists", uuid));
        if (items != null) {
            for (ListItem item : items) {
                if (item.getUuid() != null) {
                    // for now, assume they're all content
                    item.setApiUrl(String.format(API_URL_TEMPLATE, apiPath, "content", item.getUuid()));
                }
            }
        }
        if(concept != null){
            concept.setApiUrl(URI.create(String.format(API_URL_TEMPLATE, apiPath, "things", concept.getUuid().toString())));
        }
    }

    public void removePrivateFields() {
        //set to null so they aren't output, there's probably a cleverer way to do this
        setUuid(null);
        set_id(null);
        if (items != null) {
            for (ListItem item : items) {
                item.setUuid(null);
            }
        }
        if(concept != null){
            concept.setUuid(null);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("title", title)
                .add("apiUrl", apiUrl)
                .add("concept", concept)
                .add("listType",  listType)
                .add("items", items)
                .add("publishedDate", publishedDate)
                .add("layoutHint", layoutHint)
                .add("publishReference", publishReference)
                .add("lastModified", lastModified)
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
                && Objects.equal(this.concept, other.concept)
                && Objects.equal(this.listType, other.listType)
                && Objects.equal(this.items, other.items)
                && Objects.equal(this.publishedDate, other.publishedDate)
                && Objects.equal(this.layoutHint, other.layoutHint)
                && Objects.equal(this.publishReference, publishReference)
                && Objects.equal(this.lastModified, lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid, title, apiUrl, concept, listType, items, publishedDate, layoutHint, publishReference, lastModified);
    }

}
