package com.ft.universalpublishing.documentstore.reader;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.content.model.Brand;
import com.ft.content.model.Content;
import com.ft.content.model.ContentOrigin;
import com.ft.content.model.Identifier;
import com.ft.content.model.Member;

public class ContentWithId extends Content {

	private String _id;

    public ContentWithId(@JsonProperty("_id") String _id,
    		@JsonProperty("uuid") UUID uuid,
                   @JsonProperty("title") String title,
                   @JsonProperty("titles") List<String> titles,
                   @JsonProperty("byline") String byline,
                   @JsonProperty("brands") SortedSet<Brand> brands,
                   @JsonProperty("contentOrigin") ContentOrigin contentOrigin,
                   @JsonProperty("identifiers") SortedSet<Identifier> identifiers,
                   @JsonProperty("publishedDate") Date publishedDate,
                   @JsonProperty("body") String body,
                   @JsonProperty("description") String description,
                   @JsonProperty("mediaType") String mediaType,
                   @JsonProperty("pixelWidth") Integer pixelWidth,
                   @JsonProperty("pixelHeight") Integer pixelHeight,
                   @JsonProperty("internalBinaryUrl") String internalBinaryUrl,
                   @JsonProperty("members") SortedSet<Member> members,
                   @JsonProperty("mainImage") String mainImage) {
        
    	super(uuid, title, titles, byline, brands, contentOrigin, identifiers, publishedDate, body, description, mediaType, pixelWidth, pixelHeight, internalBinaryUrl, members, mainImage);
    	this._id = _id;
    }

    //TODO - make non serializable OR set the value to null before returning
    public String getId() {
      return _id;
    }

    //TODO - toString etc
}