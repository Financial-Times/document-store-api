package com.ft.universalpublishing.documentstore.model.transformer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class EditorialList {
    
    private List<Item> items;
    private String title;
    private String uuid;
    private OffsetDateTime publishedDate;
    private String layoutHint;
    
    private EditorialList(@JsonProperty("uuid") String uuid,
                          @JsonProperty("title")String title,
                          @JsonProperty("items") List<Item> items,
                          @JsonProperty("publishedDate") OffsetDateTime publishedDate,
                          @JsonProperty("layoutHint") String layoutHint) {
        this.uuid = uuid;
        this.title = title;
        this.items = items;
        this.publishedDate = publishedDate;
        this.layoutHint = layoutHint;
    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    @NotEmpty
    public String getTitle() {
        return title;
    }

    public List<Item> getItems() {
        return items;
    }

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
    public OffsetDateTime getPublishedDate() {
        return publishedDate;
    }
    
    @JsonInclude(NON_EMPTY)
    public String getLayoutHint() {
        return layoutHint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditorialList that = (EditorialList) o;
        return Objects.equal(this.uuid, that.uuid)
                && Objects.equal(this.title, that.title)
                && Objects.equal(this.items, that.items)
                && Objects.equal(this.publishedDate, that.publishedDate)
                && Objects.equal(this.layoutHint, that.layoutHint);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid, title, items, publishedDate, layoutHint);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("title", title)
                .add("items", items)
                .add("publishedDate", publishedDate)
                .add("layoutHint", layoutHint)
                .toString();
    }

    public static class Builder {

        private UUID uuid;
        private String title;
        private List<Item> items;
        private OffsetDateTime publishedDate;
        private String layoutHint;

        public Builder withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withItems(List<Item> items) {
            this.items = items;
            return this;
        }

        public Builder withPublishedDate(OffsetDateTime publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder withLayoutHint(String layoutHint) {
            this.layoutHint = layoutHint;
            return this;
        }

        public EditorialList build() {
            return new EditorialList(uuid.toString(), title, items, publishedDate, layoutHint);
        }
    }
}
