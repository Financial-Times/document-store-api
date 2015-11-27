package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessor;
import com.ft.bodyprocessing.BodyProcessorChain;
import com.ft.bodyprocessing.xml.StAXTransformingBodyProcessor;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

import java.util.Collections;
import java.util.List;

public class ModelBodyXmlTransformer {

    private final UriBuilder uriBuilder;

    public ModelBodyXmlTransformer(final UriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    public String transform(final String body, final LinkProcessingContext context) {
        return new BodyProcessorChain(bodyProcessors(context)).process(body, context);
    }

    private List<BodyProcessor> bodyProcessors(final LinkProcessingContext context) {
        final XMLEventHandlerRegistry registry = new ModelBodyTransformationXmlEventHandlerRegistry(uriBuilder, context);
        return Collections.singletonList(new StAXTransformingBodyProcessor(registry));
    }
}
