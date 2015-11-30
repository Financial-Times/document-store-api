package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

public class ModelBodyTransformationXmlEventHandlerRegistry extends XMLEventHandlerRegistry {

	public ModelBodyTransformationXmlEventHandlerRegistry(final UriBuilder uriBuilder, final DocumentProcessingContext context) {
		registerDefaultEventHandler(new RetainXMLEventHandler());
		registerStartAndEndElementEventHandler(new RewriteLinkXMLEventHandler("ft-content",uriBuilder, context), "content");
        registerStartAndEndElementEventHandler(new RewriteLinkXMLEventHandler("ft-related",uriBuilder, context), "related");
	}
}
