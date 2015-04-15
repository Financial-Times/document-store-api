package com.ft.universalpublishing.documentstore.model;

import java.util.Date;
import java.util.SortedSet;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({"id", "uuid", "type", "bodyXML", "title", "byline", "publishedDate", "identifiers", "requestUrl", "brands", "mainImage"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Content extends Document {

	private String id;
	private String uuid;
	private String type;
	private String bodyXML;
	private String title;
	private String byline;
	private Date publishedDate;
	private SortedSet<Identifier> identifiers;
	private String requestUrl;
	private String webUrl;
	private SortedSet<String> brands; //TODO - this should actually be output as a list of Brand objects but this is a breaking API change 
	private MainImage mainImage; 

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

	public String getBodyXML() {
		return bodyXML;
	}

	public void setBodyXML(String bodyXML) {
		this.bodyXML = bodyXML;
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

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

	public void setBrands(SortedSet<String> brands) {
		this.brands = brands;
	}

	public SortedSet<String> getBrands() {
		return brands;
	}

	public SortedSet<Identifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(SortedSet<Identifier> identifiers) {
		this.identifiers = identifiers;
	}

    public MainImage getMainImage() {
        return mainImage;
    }

    public void setMainImage(MainImage mainImage) {
        this.mainImage = mainImage;
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
                .add("bodyXML", bodyXML)
                .add("title", title)
                .add("byline", byline)
                .add("publishedDate", publishedDate)
                .add("identifiers", identifiers)
                .add("requestUrl", requestUrl)
                .add("webUrl", webUrl)
                .add("brands", brands)
                .add("mainImage", mainImage)
                .toString();
                
    }
    
    @Override
    public boolean equals(Object obj) {
 
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Content other = (Content) obj;
        return Objects.equal(this.id, other.id)
            && Objects.equal(this.uuid, other.uuid)
            && Objects.equal(this.type, other.type)
            && Objects.equal(this.bodyXML, other.bodyXML)
            && Objects.equal(this.byline, other.byline)
            && Objects.equal(this.publishedDate, other.publishedDate)
            && Objects.equal(this.identifiers, other.identifiers)
            && Objects.equal(this.requestUrl, other.requestUrl)
            && Objects.equal(this.webUrl, other.webUrl)
            && Objects.equal(this.brands, other.brands)
            && Objects.equal(this.mainImage, other.mainImage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id, uuid, type, bodyXML, byline, publishedDate, identifiers, requestUrl, webUrl, brands, mainImage);
    }
}
