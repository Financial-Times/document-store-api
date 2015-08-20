package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;

public class ContentBodyProcessingService {

    final ModelBodyXmlTransformer transformer;

    public ContentBodyProcessingService(final ModelBodyXmlTransformer transformer) {
        this.transformer = transformer;
    }

    public Content process(final Content content, final ApiUriGenerator currentUriGenerator) {
        return transformer.transform(content, new ContentBodyProcessingContext(currentUriGenerator));
    }
}
