package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Content;

import java.util.SortedSet;
import java.util.TreeSet;

public class BrandInferrer {

    private static final String FT_BRAND = "http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54";

    public Content infer(final Content source) {
        Content.Builder builder = new Content.Builder()
                .withId(source.getId())
                .withType(source.getType())
                .withTitle(source.getTitle())
                .withDescription(source.getDescription())
                .withBodyXml(source.getBodyXML())
                .withByline(source.getByline())
                .withPublishedDate(source.getPublishedDate())
                .withRequestUrl(source.getRequestUrl())
                .withBinaryUrl(source.getBinaryUrl())
                .withPublishReference(source.getPublishReference())
                .withIdentifiers(source.getIdentifiers())
                .withMembers(source.getMembers())
                .withMainImage(source.getMainImage())
                .withComments(source.getComments());

        final SortedSet<String> targetBrands = new TreeSet<>();
        if (source.getBrands() != null) {
            final SortedSet<String> sourceBrands = source.getBrands();
            targetBrands.addAll(sourceBrands);

        }
        targetBrands.add(FT_BRAND);

        builder.withBrands(targetBrands);
        return builder.build();
    }
}
