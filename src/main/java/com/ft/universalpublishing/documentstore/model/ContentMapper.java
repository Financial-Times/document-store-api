package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Comments;
import com.ft.universalpublishing.documentstore.model.read.Uri;
import com.ft.universalpublishing.documentstore.model.transformer.Brand;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import org.joda.time.DateTime;

import java.util.TreeSet;
import java.util.stream.Collectors;

public class ContentMapper {

    public static final String THING = "http://www.ft.com/thing/";
    public static final String TYPE_PREFIX = "http://www.ft.com/ontology/content/";
    public static final String API_URL_PREFIX = "http://int.api.ft.com/";

    final IdentifierMapper identifierMapper;

    public ContentMapper(final IdentifierMapper identifierMapper) {
        this.identifierMapper = new IdentifierMapper();
    }

    public com.ft.universalpublishing.documentstore.model.read.Content map(Content source) {
        return new com.ft.universalpublishing.documentstore.model.read.Content.Builder()
                .withId(THING + source.getUuid())
                .withType(API_URL_PREFIX + source.getMediaType())
                .withTitle(source.getTitle())
                .withDescription(source.getDescription())
                .withBodyXml(source.getBody())
                .withByline(source.getByline())
                .withBrands(source.getBrands().stream().map(Brand::getId).collect(Collectors.toCollection(TreeSet::new)))
                .withPublishedDate(new DateTime(source.getPublishedDate().getTime()))
                .withIdentifiers(source.getIdentifiers().stream().map(identifierMapper::map).collect(Collectors.toCollection(TreeSet::new)))
                .withMembers(source.getMembers().stream().map(member -> new Uri(member.getUuid())).collect(Collectors.toCollection(TreeSet::new)))
                .withRequestUrl(API_URL_PREFIX + source.getUuid())
                .withMainImage(new Uri(source.getMainImage()))
                .withComments(new Comments(source.getComments().isEnabled()))
                .build();
    }
}
