package com.ft.universalpublishing.documentstore.transform;

import static com.ft.universalpublishing.documentstore.model.ContentMapper.THING;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.model.read.Standout;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ft.universalpublishing.documentstore.model.read.Comments;
import com.ft.universalpublishing.documentstore.model.read.Content;


@RunWith(MockitoJUnitRunner.class)
public class ContentBodyProcessingServiceTest {
    private ContentBodyProcessingService service;
    @Mock
    private ModelBodyXmlTransformer transformer;
    
    @Before
    public void setUp() {
        service = new ContentBodyProcessingService(transformer);
    }
    
    @Test
    public void thatContentIsProcessed() {
        final String uuid = UUID.randomUUID().toString();
        final DateTime publishDate = DateTime.now();
        final DateTime lastModified = DateTime.now();
        final String body = "Why did the chicken cross the street?";
        final String opening = "Why did the chicken";
        final SortedSet<com.ft.universalpublishing.documentstore.model.read.Identifier> identifiers = new TreeSet<>();
        identifiers.add(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1"));
        final SortedSet<String> brands = new TreeSet<>();
        brands.add("Lex");
        brands.add("Chuck Taylor");
        final Standout standout = new Standout(true, true, true);
        
        when(transformer.transform(eq(body), any(DocumentProcessingContext.class))).thenReturn(body);
        when(transformer.transform(eq(opening), any(DocumentProcessingContext.class))).thenReturn(opening);
        
        final Content content = (new Content.Builder())
                .withId(THING + uuid)
                .withTitle("Philosopher")
                .withPublishedDate(publishDate)
                .withBodyXml(body)
                .withOpeningXml(opening)
                .withByline("David Jules")
                .withBrands(brands)
                .withIdentifiers(identifiers)
                .withComments(new Comments(true))
                .withPublishReference("Publish Reference")
                .withLastModifiedDate(lastModified)
                .withStandout(standout)
                .build();
        
        Content actual = service.process(content, null);
        
        verify(transformer).transform(eq(body), any(DocumentProcessingContext.class));
        verify(transformer).transform(eq(opening), any(DocumentProcessingContext.class));
        
        assertThat(actual.getId(), equalTo(THING + uuid));
        assertThat(actual.getTitle(), equalTo("Philosopher"));
        assertThat(actual.getPublishedDate(), equalTo(publishDate));
        assertThat(actual.getBodyXML(), equalTo(body));
        assertThat(actual.getOpeningXML(), equalTo(opening));
        assertThat(actual.getByline(), equalTo("David Jules"));
        assertThat(actual.getIdentifiers().first(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Identifier("authority1", "identifier1")));
        assertThat(actual.getBrands(), equalTo(brands));
        assertThat(actual.getComments(), equalTo(new com.ft.universalpublishing.documentstore.model.read.Comments(true)));
        assertThat(actual.getPublishReference(), equalTo("Publish Reference"));
        assertThat(actual.getLastModified(), equalTo(lastModified));
        assertThat(actual.getStandout(), equalTo(standout));
    }
}
