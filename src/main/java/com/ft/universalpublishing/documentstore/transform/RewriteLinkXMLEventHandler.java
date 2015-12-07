package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.bodyprocessing.writer.BodyWriter;
import com.ft.bodyprocessing.xml.eventhandlers.BaseXMLEventHandler;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import com.google.common.base.Preconditions;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.LinkedHashMap;
import java.util.Map;

public class RewriteLinkXMLEventHandler extends BaseXMLEventHandler {

    public static final String TYPE_ATTRIBUTE_NAME = "type";
    public static final String ID_ATTRIBUTE_NAME = "id";
    public static final String URL_ATTRIBUTE_NAME = "url";

    public static final String TYPE_ATTR_MISSING_MESSAGE = "Type attribute is missing from a content element";
    public static final String ID_ATTR_MISSING_MESSAGE = "Id attribute is missing from a content element";

    private final UriBuilder uriBuilder;
    private final String rewriteElementName;
    private final DocumentProcessingContext context;

    public RewriteLinkXMLEventHandler(String rewriteElementName, UriBuilder uriBuilder, DocumentProcessingContext context) {
        this.rewriteElementName = rewriteElementName;
        this.uriBuilder = uriBuilder;
        this.context = context;
    }

    public DocumentProcessingContext getContext() {
        return this.context;
    }

    public String getRewriteElementName() {
        return rewriteElementName;
    }

    @Override
    public void handleStartElementEvent(final StartElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter,
                                        final BodyProcessingContext bodyProcessingContext) throws XMLStreamException {
        if (getContext().isProcessingLink()) {
            throw new BodyTransformationException("Nested content is not permitted");
        } else {
            getContext().setProcessingLink(true);
        }

        /* we expect a new instance of this class for every run through the transform and for the same
        context to get to us twice (once with additional type information, and again to satisfy an interface
         */
        Preconditions.checkArgument(bodyProcessingContext == context);

        final Map<String, String> remaining = removeFtContentAttributes(event);

        final String type = getAttribute(event, TYPE_ATTRIBUTE_NAME, TYPE_ATTR_MISSING_MESSAGE);
        final String id = getAttribute(event, ID_ATTRIBUTE_NAME, ID_ATTR_MISSING_MESSAGE);

        ApiUriGenerator uriGenerator = context.getUriGenerator();

        try {
            final String mergedUrl = uriBuilder.mergeUrl(type,id);

            final Map<String, String> rewrittenAttributes = new LinkedHashMap<>();
            rewrittenAttributes.put(TYPE_ATTRIBUTE_NAME, type);
            rewrittenAttributes.put(URL_ATTRIBUTE_NAME, uriGenerator.resolve(mergedUrl));
            rewrittenAttributes.putAll(remaining);
            eventWriter.writeStartTag(getRewriteElementName(), rewrittenAttributes);

        } catch (final Exception e) {
            throw new BodyTransformationException("Failed to rewrite the body", e);
        }
    }

    @Override
    public void handleEndElementEvent(final EndElement event, final XMLEventReader xmlEventReader, final BodyWriter eventWriter) throws XMLStreamException {
        eventWriter.writeEndTag(getRewriteElementName());
        context.setProcessingLink(false);
    }

    private String getAttribute(final StartElement event, final String name, final String missingValueErrorMessage) {
        final Attribute namedAttribute = event.getAttributeByName(new QName(name));
        if (namedAttribute == null) {
            throw new BodyTransformationException(missingValueErrorMessage);
        }

        final String value = namedAttribute.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new BodyTransformationException(missingValueErrorMessage);
        }

        return value;
    }

    private Map<String, String> removeFtContentAttributes(final StartElement event) {
        final Map<String, String> existing = getValidAttributesAndValues(event);
        if (existing == null) {
            return new LinkedHashMap<>();
        } else {
            existing.remove(ID_ATTRIBUTE_NAME);
            existing.remove(TYPE_ATTRIBUTE_NAME);
            existing.remove(URL_ATTRIBUTE_NAME);
            return existing;
        }
    }
}
