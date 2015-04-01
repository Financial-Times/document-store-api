package com.ft.universalpublishing.documentstore.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ft.ws.lib.serialization.datetime.JsonDateTimeWithMillisSerializer;

import org.joda.time.DateTime;

import java.util.Set;
import java.util.SortedSet;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "type", "bodyXML", "title", "byline", "description", "publishedDate", "contentOrigin", "identifiers", "members", "requestUrl", "binaryUrl", "brands", "annotations"})

public class Content {

	private String id;
	private String uuid;
	private String type;
	private String bodyXml;
	private String title;
	private String byline;
	private String description;
	private DateTime publishedDate;
	private ContentOrigin contentOrigin;
	private SortedSet<Identifier> identifiers;
	private SortedSet<String> members;
	private String requestUrl;
	private String binaryUrl;
	private SortedSet<String> brands;
	private Set<Annotation> annotations;

    public Content() {
    }

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

	public ContentOrigin getContentOrigin() {
		return contentOrigin;
	}

	public void setContentOrigin(ContentOrigin contentOrigin) {
		this.contentOrigin = contentOrigin;
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

	public SortedSet<String> getMembers() {
		return members;
	}

	public void setMembers(SortedSet<String> members) {
		this.members = members;
	}

	public String getBinaryUrl() {
		return binaryUrl;
	}

	public void setBinaryUrl(String binaryUrl) {
		this.binaryUrl = binaryUrl;
	}
}
