package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Comments;
import com.ft.universalpublishing.documentstore.model.read.Content.Builder;
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
        this.identifierMapper = identifierMapper;
    }

    public com.ft.universalpublishing.documentstore.model.read.Content map(Content source) {
        Builder builder = new Builder()
                .withId(THING + source.getUuid())
                .withTitle(source.getTitle())
                .withDescription(source.getDescription())
                .withBodyXml(source.getBody())
                .withByline(source.getByline())
                .withPublishedDate(new DateTime(source.getPublishedDate().getTime()))
                .withRequestUrl(API_URL_PREFIX + source.getUuid());
        if (source.getMediaType() != null) {
            builder = builder.withType(TYPE_PREFIX + source.getMediaType());
        }
        if (source.getBrands() != null) {
            builder = builder.withBrands(source.getBrands().stream().map(Brand::getId).collect(Collectors.toCollection(TreeSet::new)));
        }
        if (source.getIdentifiers() != null) {
            builder = builder.withIdentifiers(source.getIdentifiers().stream().map(identifierMapper::map).collect(Collectors.toCollection(TreeSet::new)));
        }
        if (source.getMembers() != null) {
            builder = builder.withMembers(source.getMembers().stream().map(member -> new Uri(member.getUuid())).collect(Collectors.toCollection(TreeSet::new)));
        }
        if (source.getMainImage() != null) {
            builder = builder.withMainImage(new Uri(source.getMainImage()));
        }
        if (source.getComments() != null) {
            builder = builder.withComments(new Comments(source.getComments().isEnabled()));
        }
        return builder.build();
    }
}
