package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Uri;
import com.ft.universalpublishing.documentstore.model.transformer.Brand;
import com.ft.universalpublishing.documentstore.model.transformer.Comments;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.model.transformer.Identifier;
import com.ft.universalpublishing.documentstore.model.transformer.Member;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ContentMapperTest {
    private static final Identifier IDENTIFIER = new Identifier("authority1", "identifier1");
    private static final String FT_BRAND_URI = "http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54";
    
    private final ContentMapper mapper = new ContentMapper(new IdentifierMapper(), new TypeResolver(), new BrandsMapper(), "localhost");

    @Test
    public void testContentMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final String title = "Philosopher";
        final String byline = "David Jules";
        final String body = "Why did the chicken cross the street?";
        
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(IDENTIFIER);
        
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));
        
        final UUID mainImageUuid = UUID.randomUUID();
        
        final String ref = "Publish Reference";
        
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle(title)
                .withPublishedDate(publishDate)
                .withBody(body)
                .withByline(byline)
                .withBrands(brands)
                .withMainImage(mainImageUuid.toString())
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .withPublishReference(ref)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        final SortedSet<String> expectedBrands = new TreeSet<>();
        brands.stream().forEach(b -> expectedBrands.add(b.getId()));
        expectedBrands.add(FT_BRAND_URI);
        
        verifyContent(readContent, uuid, title, byline, publishDate, body, IDENTIFIER,
                expectedBrands, mainImageUuid, true, null, ref);
    }
    
    private void verifyContent(com.ft.universalpublishing.documentstore.model.read.Content actual,
            UUID expectedUuid, String expectedTitle, String expectedByline, Date expectedDate, String expectedBody,
            Identifier expectedIdentifier, SortedSet<String> expectedBrands,
            UUID expectedImageUuid, Boolean expectedCommentingEnabled, Boolean expectedRealtime, String expectedRef) {
        
        assertThat(actual.getId(), equalTo("http://www.ft.com/thing/" + expectedUuid.toString()));
        assertThat(actual.getTitle(), equalTo(expectedTitle));
        assertThat(actual.getPublishedDate(), equalTo(new DateTime(expectedDate.getTime())));
        assertThat(actual.getType(), equalTo(TypeResolver.TYPE_ARTICLE));
        assertThat(actual.getBodyXML(), equalTo(expectedBody));
        assertThat(actual.getByline(), equalTo(expectedByline));
        
        com.ft.universalpublishing.documentstore.model.read.Identifier actualIdentifier = actual.getIdentifiers().first();
        assertThat(actualIdentifier.getAuthority(), equalTo(expectedIdentifier.getAuthority()));
        assertThat(actualIdentifier.getIdentifierValue(), equalTo(expectedIdentifier.getIdentifierValue()));
        
        assertThat(actual.getBrands(), equalTo(expectedBrands));
        assertThat(actual.getMainImage(), equalTo(new Uri("http://localhost/content/" + expectedImageUuid.toString())));
        assertThat(actual.getComments().isEnabled(), equalTo(expectedCommentingEnabled));
        
        if (expectedRealtime == null) {
            assertThat(actual.isRealtime(), nullValue());
        }
        else {
            assertThat(actual.isRealtime(), equalTo(expectedRealtime));
        }
        
        assertThat(actual.getPublishReference(), equalTo(expectedRef));
    }
    
    @Test
    public void testLiveBlogContentMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final String title = "Philosopher";
        final String byline = "David Jules";
        
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(IDENTIFIER);
        
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("Lex"));
        brands.add(new Brand("Chuck Taylor"));
        
        final UUID mainImageUuid = UUID.randomUUID();
        
        final String ref = "Publish Reference";
        
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle(title)
                .withPublishedDate(publishDate)
                .withByline(byline)
                .withBrands(brands)
                .withMainImage(mainImageUuid.toString())
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .withRealtime(true)
                .withPublishReference(ref)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);
        
        final SortedSet<String> expectedBrands = new TreeSet<>();
        brands.stream().forEach(b -> expectedBrands.add(b.getId()));
        expectedBrands.add(FT_BRAND_URI);
        
        verifyContent(readContent, uuid, title, byline, publishDate, null, IDENTIFIER,
                expectedBrands, mainImageUuid, true, true, ref);
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
                .withPixelWidth(1536)
                .withPixelHeight(1538)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_MEDIA_RESOURCE));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getBinaryUrl() , equalTo("http://ft.s3.aws/" + uuid.toString()));
        assertThat(readContent.getPixelWidth() , equalTo(1536));
        assertThat(readContent.getPixelHeight() , equalTo(1538));
    }

    @Test
    public void testImageSetMapping() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final Date publishDate = new Date();
        final SortedSet<Identifier> identifiers = new TreeSet<>();
        identifiers.add(new Identifier("authority1", "identifier1"));
        final SortedSet<Member> members = new TreeSet<>();
        final UUID memberUuid = UUID.randomUUID();
        members.add(new Member(memberUuid.toString()));
        final Content content = Content.builder()
                .withUuid(uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withDescription("A question.")
                .withByline("David Jules")
                .withMembers(members)
                .withIdentifiers(identifiers)
                .build();

        final com.ft.universalpublishing.documentstore.model.read.Content readContent = mapper.map(content);

        assertThat(readContent.getId(), equalTo("http://www.ft.com/thing/" + uuid.toString()));
        assertThat(readContent.getTitle(), equalTo("Philosopher"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getType(), equalTo(TypeResolver.TYPE_IMAGE_SET));
        assertThat(readContent.getByline(), equalTo("David Jules"));
        assertThat(readContent.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(readContent.getMembers().first(), equalTo(new Uri("http://localhost/content/" + memberUuid.toString())));
    }
}
