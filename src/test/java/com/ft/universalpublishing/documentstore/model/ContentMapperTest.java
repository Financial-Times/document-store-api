package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Uri;
import com.ft.universalpublishing.documentstore.model.transformer.Brand;
import com.ft.universalpublishing.documentstore.model.transformer.Comments;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.model.transformer.Identifier;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContentMapperTest {

    private final ContentMapper mapper = new ContentMapper(new IdentifierMapper(), new TypeResolver());

    @Test
    public void testContentMaping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));
        final UUID mainImageUuid = UUID.randomUUID();
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withBody("Why did the chicken cross the street?")
                .withByline("David Jules")
                .withBrands(brands)
                .withMainImage("http://api.ft.com/content/" + mainImageUuid.toString())
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getPublishedDate(), equalTo(new DateTime(publishDate.getTime())));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_ARTICLE));
        assertThat(readContent.getBodyXml(), equalTo("Why did the chicken cross the street?"));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        final SortedSet<String> expectedBrands = new TreeSet<>();
        expectedBrands.add("Lex");
        expectedBrands.add("Chuck Taylor");
        assertThat(readContent.getBrands(), equalTo(expectedBrands));
        assertThat(readContent.getMainImage(), equalTo(new Uri("http://api.ft.com/content/" + mainImageUuid.toString())));
        assertThat(readContent.getComments(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Comments(true)));
    }

    @Test
    public void testImageMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withDescription("A question.")
                .withByline("David Jules")
                .withInternalBinaryUrl("http://methode-image-binary-transformer/binary/" + uuid.toString())
                .withExternalBinaryUrl("http://ft.s3.aws/" + uuid.toString())
                .withIdentifiers(identifiers)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_MEDIA_RESOURCE));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getBinaryUrl() , equalTo("http://ft.s3.aws/" + uuid.toString()));
    }
}