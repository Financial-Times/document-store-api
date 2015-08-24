package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

public class ModelBodyTransformationXmlEventHandlerRegistry extends XMLEventHandlerRegistry {

	public ModelBodyTransformationXmlEventHandlerRegistry(final UriBuilder uriBuilder, final ContentBodyProcessingContext context) {
		registerDefaultEventHandler(new RetainXMLEventHandler());
		registerStartAndEndElementEventHandler(new RewriteContentElementEventHandler(uriBuilder, context), "content");
	}
}
