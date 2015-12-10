package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.universalpublishing.documentstore.util.FixedUriGenerator;
import org.apache.xerces.stax.events.StartElementImpl;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RewriteRelatedElementTest {

    public static final QName RELATED = new QName("related");
    private final DocumentProcessingContext context = new DocumentProcessingContext(FixedUriGenerator.localUriGenerator());

	private RewriteLinkXMLEventHandler unit;

	@Before
	public void before() {
		final Map<String, String> templates = new LinkedHashMap<>();
		templates.put("TEST-TYPE", "/content/{{id}}");
		this.unit = new RewriteLinkXMLEventHandler("ft-related",new UriBuilder(templates), context);
	}

	@Test
	public void testRewrite() throws Exception {
		final StartElement startElement = mockStartElement();
		final XMLEventReader xmlEventReader = mock(XMLEventReader.class);
		final BodyWriter eventWriter = mock(BodyWriter.class);

		final Map<String, String> expectedAttributes = new LinkedHashMap<>();
		expectedAttributes.put("url", "http://example.com/some-stuff-online");

		unit.handleStartElementEvent(startElement, xmlEventReader, eventWriter, context);

		verify(eventWriter).writeStartTag("ft-related", expectedAttributes);

	}

	private StartElement mockStartElement() {

		final Attribute url = mockAttribute("url", "http://example.com/some-stuff-online");

		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(url);

		return new StartElementImpl(RELATED, attributes.iterator(), null, null, null);
	}

	private Attribute mockAttribute(final String name, final String value) {
		final Attribute attribute = mock(Attribute.class);
		when(attribute.getName()).thenReturn(new QName(name));
		when(attribute.getValue()).thenReturn(value);
		return attribute;
	}
}
