package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

import java.util.Collections;
import java.util.List;

public class ModelBodyXmlTransformer {

    private final UriMerger uriMerger;

    public ModelBodyXmlTransformer(final UriMerger uriMerger) {
        this.uriMerger = uriMerger;
    }

    public String transform(final String body, final ContentBodyProcessingContext context) {
        return new BodyProcessorChain(bodyProcessors(context)).process(body, context);
    }

    private List<BodyProcessor> bodyProcessors(final ContentBodyProcessingContext context) {
        final XMLEventHandlerRegistry registry = new ModelBodyTransformationXmlEventHandlerRegistry(uriMerger, context);
        return Collections.singletonList(new StAXTransformingBodyProcessor(registry));
    }
}
