package com.ft.universalpublishing.documentstore.model.transformer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {

    private final String uuid;
    private final String title;
    private final List<String> titles;
    private final String byline;
    private final SortedSet<Brand> brands;
    private final Date publishedDate;
    private final String body;
    private final String opening;
    private final SortedSet<Identifier> identifiers;
    private final String description;
    private final String mediaType;
    private final Integer pixelWidth;
    private final Integer pixelHeight;
    private final String internalBinaryUrl;
    private final String externalBinaryUrl;
    private final SortedSet<Member> members;
    private final String mainImage;
    private final Comments comments;
    private final Boolean realtime;
    private final Copyright copyright;
    private final String publishReference;
    private final Date lastModified;

    private Content(@JsonProperty("uuid") UUID uuid,
            @JsonProperty("title") String title,
            @JsonProperty("titles") List<String> titles,
            @JsonProperty("byline") String byline,
            @JsonProperty("brands") SortedSet<Brand> brands,
            @JsonProperty("identifiers") SortedSet<Identifier> identifiers,
            @JsonProperty("publishedDate") Date publishedDate,
            @JsonProperty("body") String body,
            @JsonProperty("opening") String opening,
            @JsonProperty("description") String description,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("pixelWidth") Integer pixelWidth,
            @JsonProperty("pixelHeight") Integer pixelHeight,
            @JsonProperty("internalBinaryUrl") String internalBinaryUrl,
            @JsonProperty("externalBinaryUrl") String externalBinaryUrl,
            @JsonProperty("members") SortedSet<Member> members,
            @JsonProperty("mainImage") String mainImage,
            @JsonProperty("comments") Comments comments,
            @JsonProperty("realtime") Boolean realtime,
            @JsonProperty("copyright") Copyright copyright,
            @JsonProperty("publishReference") String publishReference,
            @JsonProperty("lastModified") Date lastModified) {
        this.identifiers = identifiers;
        this.body = body;
        this.opening = opening;
        this.comments = comments;
        this.uuid = uuid == null ? null : uuid.toString();
        this.title = title;
        this.titles = titles;
        this.byline = byline;
        this.brands = brands;
        this.publishedDate = publishedDate;
        this.description = description;
        this.mediaType = mediaType;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.internalBinaryUrl = internalBinaryUrl;
        this.externalBinaryUrl = externalBinaryUrl;
        this.members = members;
        this.mainImage = mainImage;
        this.realtime = realtime;
        this.copyright = copyright;
        this.publishReference = publishReference;
        this.lastModified = lastModified;
    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    @NotEmpty
    public String getTitle() {
        return title;
    }

    public List<String> getTitles() {
        return titles;
    }

    public String getByline() {
        return byline;
    }

    public SortedSet<Brand> getBrands() {
        return brands;
    }

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getPublishedDate() {
        return publishedDate;
    }

    public String getBody() {
        return body;
    }
    
    public String getOpening() {
        return opening;
    }
    
    public SortedSet<Identifier> getIdentifiers() {
        return identifiers;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Integer getPixelWidth() {
        return pixelWidth;
    }

    public Integer getPixelHeight() {
        return pixelHeight;
    }

    public String getInternalBinaryUrl() {
        return internalBinaryUrl;
    }

    public String getExternalBinaryUrl() {
        return externalBinaryUrl;
    }

    public SortedSet<Member> getMembers() {
        return members;
    }

    public String getMainImage() {
        return mainImage;
    }

    public Comments getComments() {
        return comments;
    }

    public Boolean isRealtime() {
        return realtime;
    }

    public Copyright getCopyright() {
        return copyright;
    }

    public String getPublishReference() {
        return publishReference;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("uuid", uuid)
                .add("title", title)
                .add("byline", byline)
                .add("brands", brands)
                .add("identifiers", identifiers)
                .add("publishedDate", publishedDate)
                .add("body", body)
                .add("opening", opening)
                .add("description", description)
                .add("mediaType", mediaType)
                .add("pixelWidth", pixelWidth)
                .add("pixelHeight", pixelHeight)
                .add("internalBinaryUrl", internalBinaryUrl)
                .add("externalBinaryUrl", externalBinaryUrl)
                .add("members", members)
                .add("mainImage", mainImage)
                .add("comments", comments)
                .add("realtime", realtime)
                .add("copyright", copyright)
                .add("publishReference", publishReference)
                .add("lastModified", lastModified)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content that = (Content) o;

        return Objects.equal(this.uuid, that.uuid)
                && Objects.equal(this.title, that.title)
                && Objects.equal(this.byline, that.byline)
                && Objects.equal(this.brands, that.brands)
                && Objects.equal(this.identifiers, that.identifiers)
                && Objects.equal(this.body, that.body) // TODO maybe this could be better. The strings could be equivalent as xml even though they are different strings
                && Objects.equal(this.opening, that.opening)
                && Objects.equal(this.publishedDate, that.publishedDate)
                && Objects.equal(this.description, that.description)
                && Objects.equal(this.mediaType, that.mediaType)
                && Objects.equal(this.pixelWidth, that.pixelWidth)
                && Objects.equal(this.pixelHeight, that.pixelHeight)
                && Objects.equal(this.internalBinaryUrl, that.internalBinaryUrl)
                && Objects.equal(this.externalBinaryUrl, that.externalBinaryUrl)
                && Objects.equal(this.members, that.members)
                && Objects.equal(this.mainImage, that.mainImage)
                && Objects.equal(this.comments, that.comments)
                && Objects.equal(this.realtime, that.realtime)
                && Objects.equal(this.copyright, that.copyright)
                && Objects.equal(this.publishReference, that.publishReference)
                && Objects.equal(this.lastModified, that.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                title, 
                byline, 
                brands, 
                identifiers, 
                uuid, 
                publishedDate, 
                body, 
                opening,
                description, 
                mediaType, 
                pixelWidth, 
                pixelHeight, 
                internalBinaryUrl, 
                externalBinaryUrl, 
                members, 
                mainImage, 
                comments, 
                realtime, 
                copyright, 
                publishReference,
                lastModified
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID uuid;
        private String title;
        private List<String> titles;
        private String byline;
        private SortedSet<Brand> brands;
        private Date publishedDate;
        private String body;
        private String opening;
        private SortedSet<Identifier> identifiers;
        private String description;
        private String mediaType;
        private Integer pixelWidth;
        private Integer pixelHeight;
        private String internalBinaryUrl;
        private String externalBinaryUrl;
        private SortedSet<Member> members;
        private String mainImage;
        private Comments comments;
        private Boolean realtime;
        private Copyright copyright;
        private String transactionId;
        private Date lastModified;

        public Builder withUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withTitles(List<String> titles) {
            this.titles = titles;
            if (titles != null) {
                Collections.sort(titles, new LengthComparator());
            }
            return this;
        }

        public Builder withByline(String byline) {
            this.byline = byline;
            return this;
        }

        public Builder withBrands(SortedSet<Brand> brands) {
            this.brands = brands;
            return this;
        }

        public Builder withPublishedDate(Date publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder withBody(String body) {
            this.body = body;
            return this;
        }
        
        public Builder withOpening(String opening) {
            this.opening = opening;
            return this;
        }
        
        public Builder withIdentifiers(SortedSet<Identifier> identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder withPixelWidth(Integer pixelWidth) {
            this.pixelWidth = pixelWidth;
            return this;
        }

        public Builder withPixelHeight(Integer pixelHeight) {
            this.pixelHeight = pixelHeight;
            return this;
        }

        public Builder withInternalBinaryUrl(String internalDataUrl) {
            this.internalBinaryUrl = internalDataUrl;
            return this;
        }

        public Builder withExternalBinaryUrl(String externalBinaryUrl) {
            this.externalBinaryUrl = externalBinaryUrl;
            return this;
        }

        public Builder withMembers(SortedSet<Member> members) {
            this.members = members;
            return this;
        }

        public Builder withMainImage(String mainImage) {
            this.mainImage = mainImage;
            return this;
        }

        public Builder withComments(Comments comments) {
            this.comments = comments;
            return this;
        }

        public Builder withCopyright(Copyright copyright) {
            this.copyright = copyright;
            return this;
        }

        public Builder withRealtime(Boolean realtime) {
            this.realtime = realtime;
            return this;
        }
        
        public Builder withPublishReference(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder withLastModifiedDate(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder withValuesFrom(Content content) {
            return withTitle(content.getTitle())
                    .withTitles(content.getTitles())
                    .withByline(content.getByline())
                    .withBrands(content.getBrands())
                    .withIdentifiers(content.getIdentifiers())
                    .withUuid(UUID.fromString(content.getUuid()))
                    .withPublishedDate(content.getPublishedDate())
                    .withBody(content.getBody())
                    .withOpening(content.getOpening())
                    .withDescription(content.getDescription())
                    .withMediaType(content.getMediaType())
                    .withPixelWidth(content.getPixelWidth())
                    .withPixelHeight(content.getPixelHeight())
                    .withInternalBinaryUrl(content.getInternalBinaryUrl())
                    .withExternalBinaryUrl(content.getExternalBinaryUrl())
                    .withMembers(content.getMembers())
                    .withMainImage(content.getMainImage())
                    .withComments(content.getComments())
                    .withRealtime(content.isRealtime())
                    .withCopyright(content.getCopyright())
                    .withPublishReference(content.getPublishReference())
                    .withLastModifiedDate(content.getLastModified());
        }

		public Content build() {
            return new Content(
                    uuid, 
                    title, 
                    titles, 
                    byline, 
                    brands, 
                    identifiers, 
                    publishedDate, 
                    body, 
                    opening,
                    description, 
                    mediaType, 
                    pixelWidth, 
                    pixelHeight, 
                    internalBinaryUrl, 
                    externalBinaryUrl, 
                    members, 
                    mainImage, 
                    comments, 
                    realtime, 
                    copyright,
                    transactionId,
                    lastModified
            );
        }
    }

    private static final class LengthComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }
    }
}
