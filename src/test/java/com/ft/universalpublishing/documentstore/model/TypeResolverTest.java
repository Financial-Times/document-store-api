package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.model.transformer.Identifier;
import org.junit.Test;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeResolverTest {

    private final TypeResolver resolver = new TypeResolver();

    @Test
    public void testBodyMeansItsArticle() throws Exception {
        final Content content = Content.builder().withBody("Winter is coming.").build();
        final String type = resolver.resolveType(content);
        assertThat(type, equalTo(TypeResolver.TYPE_ARTICLE));
    }

    @Test
    public void thatRealtimeMeansItsArticle() throws Exception {
        final Content content = Content.builder().withRealtime(true).build();
        final String type = resolver.resolveType(content);
        assertThat(type, equalTo(TypeResolver.TYPE_ARTICLE));
    }

    @Test
    public void testMembersMeansItsImageSet() throws Exception {
        final Content content = Content.builder().withMembers(new TreeSet<>()).build();
        final String type = resolver.resolveType(content);
        assertThat(type, equalTo(TypeResolver.TYPE_IMAGE_SET));
    }

    @Test
    public void testInternalBinaryUrlMeansItsMediaResource() throws Exception {
        final Content content = Content.builder()
                .withInternalBinaryUrl("http://methode-image-binary-transformer:8080/binary/98434398394")
                .build();
        final String type = resolver.resolveType(content);
        assertThat(type, equalTo(TypeResolver.TYPE_MEDIA_RESOURCE));
    }

    @Test
    public void testBrightcoveAuthorityMeansItsMediaResource() throws Exception {
        final Identifier identifier = new Identifier(TypeResolver.BRIGHTCOVE_AUTHORITY, "123");
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(identifier);
        final Content content = Content.builder()
                .withIdentifiers(identifiers)
                .build();
        final String type = resolver.resolveType(content);
        assertThat(type, equalTo(TypeResolver.TYPE_MEDIA_RESOURCE));
    }
}
