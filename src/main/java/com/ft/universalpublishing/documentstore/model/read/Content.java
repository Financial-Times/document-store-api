package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ft.ws.lib.serialization.datetime.JsonDateTimeWithMillisSerializer;
import org.joda.time.DateTime;

import java.util.Set;
import java.util.SortedSet;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
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
	private SortedSet<UriResult> members;
	private String requestUrl;
	private String binaryUrl;
	private SortedSet<String> brands;
	private Set<Annotation> annotations;
    private UriResult mainImage;
    private Comments comments;

    public Content() {
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

	@JsonProperty("bodyXML")
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

    public Set<AnnotationResult> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<AnnotationResult> annotations) {
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

	public SortedSet<UriResult> getMembers() {
		return members;
	}

	public void setMembers(SortedSet<UriResult> members) {
		this.members = members;
	}

	public String getBinaryUrl() {
		return binaryUrl;
	}

	public void setBinaryUrl(String binaryUrl) {
		this.binaryUrl = binaryUrl;
	}

    public UriResult getMainImage() {
        return mainImage;
    }

    public void setMainImage(UriResult mainImage) {
        this.mainImage = mainImage;
    }

    public CommentsResult getComments() {
        return comments;
    }

    public void setComments(CommentsResult comments) {
        this.comments = comments;
    }
}
