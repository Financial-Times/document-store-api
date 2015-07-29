package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ft.ws.lib.serialization.datetime.JsonDateTimeWithMillisSerializer;
import org.joda.time.DateTime;

import java.util.Set;
import java.util.SortedSet;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"id", "type", "bodyXML", "title", "byline", "description", "publishedDate", "identifiers", "members", "requestUrl", "binaryUrl", "brands", "annotations", "mainImage", "comments"})
public class Content {

	private String id;
	private String type;
	private String bodyXml;
	private String title;
	private String byline;
	private String description;
	private DateTime publishedDate;
	private SortedSet<Identifier> identifiers;
	private SortedSet<Uri> members;
	private String requestUrl;
	private String binaryUrl;
	private SortedSet<String> brands;
	private Set<Annotation> annotations;
    private Uri mainImage;
    private Comments comments;

    public Content() {
    }

    public Content(@JsonProperty("id") String id,
                   @JsonProperty("type") String type,
                   @JsonProperty("bodyXml") String bodyXml,
                   @JsonProperty("title") String title,
                   @JsonProperty("byline") String byline,
                   @JsonProperty("description") String description,
                   @JsonProperty("publishedDate") DateTime publishedDate,
                   @JsonProperty("identifiers") SortedSet<Identifier> identifiers,
                   @JsonProperty("members") SortedSet<Uri> members,
                   @JsonProperty("requestUrl") String requestUrl,
                   @JsonProperty("binaryUrl") String binaryUrl,
                   @JsonProperty("brands") SortedSet<String> brands,
                   @JsonProperty("annotations") Set<Annotation> annotations,
                   @JsonProperty("mainImage") Uri mainImage,
                   @JsonProperty("comments") Comments comments) {
        this.id = id;
        this.type = type;
        this.bodyXml = bodyXml;
        this.title = title;
        this.byline = byline;
        this.description = description;
        this.publishedDate = publishedDate;
        this.identifiers = identifiers;
        this.members = members;
        this.requestUrl = requestUrl;
        this.binaryUrl = binaryUrl;
        this.brands = brands;
        this.annotations = annotations;
        this.mainImage = mainImage;
        this.comments = comments;
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBodyXml() {
		return bodyXml;
	}

	public void setBodyXml(String bodyXml) {
		this.bodyXml = bodyXml;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getByline() {
		return byline;
	}

	public void setByline(String byline) {
		this.byline = byline;
	}

    @JsonSerialize(using = JsonDateTimeWithMillisSerializer.class)
	public DateTime getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(DateTime publishedDate) {
		this.publishedDate = publishedDate;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setBrands(SortedSet<String> brands) {
		this.brands = brands;
	}

	public SortedSet<String> getBrands() {
		return brands;
	}

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SortedSet<Identifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(SortedSet<Identifier> identifiers) {
		this.identifiers = identifiers;
	}

	public SortedSet<Uri> getMembers() {
		return members;
	}

	public void setMembers(SortedSet<Uri> members) {
		this.members = members;
	}

	public String getBinaryUrl() {
		return binaryUrl;
	}

	public void setBinaryUrl(String binaryUrl) {
		this.binaryUrl = binaryUrl;
	}

    public Uri getMainImage() {
        return mainImage;
    }

    public void setMainImage(Uri mainImage) {
        this.mainImage = mainImage;
    }

    public Comments getComments() {
        return comments;
    }

    public void setComments(Comments comments) {
        this.comments = comments;
    }

    public static class Builder {

        private String id;
        private String type;
        private String bodyXml;
        private String title;
        private String byline;
        private String description;
        private DateTime publishedDate;
        private SortedSet<Identifier> identifiers;
        private SortedSet<Uri> members;
        private String requestUrl;
        private String binaryUrl;
        private SortedSet<String> brands;
        private Set<Annotation> annotations;
        private Uri mainImage;
        private Comments comments;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withBodyXml(String bodyXml) {
            this.bodyXml = bodyXml;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withByline(String byline) {
            this.byline = byline;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withPublishedDate(DateTime publishedDate) {
            this.publishedDate = publishedDate;
            return this;
        }

        public Builder withIdentifiers(SortedSet<Identifier> identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public Builder withMembers(SortedSet<Uri> members) {
            this.members = members;
            return this;
        }

        public Builder withRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public Builder withBrands(SortedSet<String> brands) {
            this.brands = brands;
            return this;
        }

        public Builder withAnnotations(Set<Annotation> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder withMainImage(Uri mainImage) {
            this.mainImage = mainImage;
            return this;
        }

        public Builder withComments(Comments comments) {
            this.comments = comments;
            return this;
        }

        public Content build() {
            return new Content(id,
                    type,
                    bodyXml,
                    title,
                    byline,
                    description,
                    publishedDate,
                    identifiers,
                    members,
                    requestUrl,
                    binaryUrl,
                    brands,
                    annotations,
                    mainImage,
                    comments
            );
        }
    }
}
