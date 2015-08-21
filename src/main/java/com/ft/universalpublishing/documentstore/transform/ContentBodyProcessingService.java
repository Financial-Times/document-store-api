package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;

public class ContentBodyProcessingService {

    final ModelBodyXmlTransformer transformer;

    public ContentBodyProcessingService(final ModelBodyXmlTransformer transformer) {
        this.transformer = transformer;
    }

    public Content process(final Content content, final ApiUriGenerator currentUriGenerator) {
        if (content.getBodyXML() == null) {
            return content;
        }
        final String transformedBody = transformer.transform(content.getBodyXML(), new ContentBodyProcessingContext(currentUriGenerator));
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
}
