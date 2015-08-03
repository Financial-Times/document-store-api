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
    public static final String API_URL_PREFIX = "http://int.api.ft.com/";

    private final IdentifierMapper identifierMapper;
    private final TypeResolver typeResolver;

    public ContentMapper(final IdentifierMapper identifierMapper,
                         final TypeResolver typeResolver) {
        this.identifierMapper = identifierMapper;
        this.typeResolver = typeResolver;
    }

    public com.ft.universalpublishing.documentstore.model.read.Content map(final Content source) {
        Builder builder = new Builder()
                .withId(THING + source.getUuid())
                .withType(typeResolver.resolveType(source))
                .withTitle(source.getTitle())
                .withDescription(source.getDescription())
                .withBodyXml(source.getBody())
                .withByline(source.getByline())
                .withPublishedDate(new DateTime(source.getPublishedDate().getTime()))
                .withRequestUrl(API_URL_PREFIX + source.getUuid());
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
        if (source.getExternalBinaryUrl() != null) {
            builder = builder.withBinaryUrl(source.getExternalBinaryUrl());
        }
        if (source.getComments() != null) {
            builder = builder.withComments(new Comments(source.getComments().isEnabled()));
        }
        return builder.build();
    }
}
