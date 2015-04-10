package com.ft.universalpublishing.documentstore.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "type", "bodyXML", "title", "byline", "description", "publishedDate", "identifiers", "members", "requestUrl", "binaryUrl", "brands", "annotations"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Content extends Document {

	private String id;
	private String uuid;
	private String type;
	private String bodyXml;
	private String title;
	private String byline;
	private String description;
	private Date publishedDate;
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
	
	@NotNull
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

	public Date getPublishedDate() {
		return publishedDate;
	}

	@JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="UTC")
	public void setPublishedDate(Date publishedDate) {
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
	

    @Override
    public void addIds() {
        setId(IDENTIFIER_TEMPLATE + uuid);
    }

    @Override
    public void addApiUrls(String apiPath) {
        setRequestUrl(String.format(API_URL_TEMPLATE, apiPath, "content", uuid));
    }

    @Override
    public void removePrivateFields() {
        //set to null so they aren't output, there's probably a cleverer way to do this
        setUuid(null);
        set_id(null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("type", type)
                .add("bodyXml", bodyXml)
                .add("title", title)
                .add("byline", byline)
                .add("description", description)
                .add("publishedDate", publishedDate)
                .add("identifiers", identifiers)
                .add("members", members)
                .add("requestUrl", requestUrl)
                .add("binaryUrl", binaryUrl)
                .add("brands", brands)
                .add("annotations", annotations)
                .toString();
                
    }
    
    @Override
    public boolean equals(Object obj) {
 
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Content other = (Content) obj;
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
