package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;
import com.ft.universalpublishing.documentstore.model.read.Content;

import java.util.Collections;
import java.util.List;

public class ModelBodyXmlTransformer {

    private final UriMerger uriMerger;

    public ModelBodyXmlTransformer(final UriMerger uriMerger) {
        this.uriMerger = uriMerger;
    }

    public Content transform(final Content content, final ContentBodyProcessingContext context) {
        if (content.getBodyXML() == null) {
            return content;
        }
        final String transformedBody = transformBody(content.getBodyXML(), context);
        return buildContentWithNewBody(content, transformedBody);
    }

    private Content buildContentWithNewBody(final Content content, final String transformedBody) {
        return new Content.Builder()
                .withId(content.getId())
                .withType(content.getType())
                .withPublishedDate(content.getPublishedDate())
                .withByline(content.getByline())
                .withDescription(content.getDescription())
                .withIdentifiers(content.getIdentifiers())
                .withBodyXml(transformedBody)
                .withBinaryUrl(content.getBinaryUrl())
                .withMembers(content.getMembers())
                .withRequestUrl(content.getRequestUrl())
                .withMainImage(content.getMainImage())
                .withComments(content.getComments())
                .withBrands(content.getBrands())
                .withAnnotations(content.getAnnotations())
                .withPublishReference(content.getPublishReference())
                .build();
    }

	private String transformBody(final String body, ContentBodyProcessingContext context) {
        return new BodyProcessorChain(bodyProcessors(context)).process(body, context);
	}

    private List<BodyProcessor> bodyProcessors(final ContentBodyProcessingContext context) {
        final XMLEventHandlerRegistry registry = new ModelBodyTransformationXmlEventHandlerRegistry(uriMerger, context);
        return Collections.singletonList(new StAXTransformingBodyProcessor(registry));
    }
}
