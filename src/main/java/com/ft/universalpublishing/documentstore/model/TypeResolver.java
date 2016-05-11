package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Content;

public class TypeResolver {

    static final String BRIGHTCOVE_AUTHORITY = "http://api.ft.com/system/BRIGHTCOVE";

    static final String TYPE_ARTICLE = "http://www.ft.com/ontology/content/Article";
    static final String TYPE_IMAGE_SET = "http://www.ft.com/ontology/content/ImageSet";
    static final String TYPE_MEDIA_RESOURCE = "http://www.ft.com/ontology/content/MediaResource";

    String resolveType(Content source) {
        if ((source.getBody() != null)
                || ((source.isRealtime() != null) && source.isRealtime().booleanValue())) {
            return TYPE_ARTICLE;
        }
        if (source.getInternalBinaryUrl() != null || isBrightcoveAuthority(source)) {
            return TYPE_MEDIA_RESOURCE;
        }
        if (source.getMembers() != null) {
            return TYPE_IMAGE_SET;
        }
        return null;
    }

    boolean isBrightcoveAuthority(final Content source) {
        return source.getIdentifiers() != null &&
                !source.getIdentifiers().isEmpty() &&
                source.getIdentifiers().first().getAuthority().equals(BRIGHTCOVE_AUTHORITY);
    }
}
