package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Content;

public class TypeResolver {

    public static final String TYPE_ARTICLE = "http://www.ft.com/ontology/content/Article";
    public static final String TYPE_IMAGE_SET = "http://www.ft.com/ontology/content/ImageSet";
    public static final String TYPE_MEDIA_RESOURCE = "http://www.ft.com/ontology/content/MediaResource";

    public String resolveType(Content source) {
        if (source.getBody() != null) {
            return TYPE_ARTICLE;
        }
        if (source.getInternalBinaryUrl() != null) {
            return TYPE_MEDIA_RESOURCE;
        }
        if (source.getMembers() != null) {
            return TYPE_IMAGE_SET;
        }
        return null;
    }
}
