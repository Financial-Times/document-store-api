package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.xml.eventhandlers.RetainXMLEventHandler;
import com.ft.bodyprocessing.xml.eventhandlers.XMLEventHandlerRegistry;

public class ModelBodyTransformationXmlEventHandlerRegistry extends XMLEventHandlerRegistry {

	public ModelBodyTransformationXmlEventHandlerRegistry(final UriMerger uriMerger, final ContentBodyProcessingContext context) {
		registerDefaultEventHandler(new RetainXMLEventHandler());
		registerStartAndEndElementEventHandler(new RewriteContentElementEventHandler(uriMerger, context), "content");
	}
}
