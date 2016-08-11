package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import com.google.common.base.Strings;


public class ContentBodyProcessingService {

    private final ModelBodyXmlTransformer transformer;

    public ContentBodyProcessingService(final ModelBodyXmlTransformer transformer) {
        this.transformer = transformer;
    }

    public Content process(final Content content, final ApiUriGenerator currentUriGenerator) {
        if (content.getBodyXML() == null) {
            return content;
        }
        final String transformedBody = transformer.transform(content.getBodyXML(), new DocumentProcessingContext(currentUriGenerator));
        
        String openingXml = content.getOpeningXML();
        if (!Strings.isNullOrEmpty(openingXml)) {
            openingXml = transformer.transform(openingXml, new DocumentProcessingContext(currentUriGenerator));
        }
        
        return buildContentWithNewBody(content, transformedBody, openingXml);
    }

    private Content buildContentWithNewBody(final Content content,
                                            final String transformedBody, final String transformedOpening) {
        
        return new Content.Builder()
                .withId(content.getId())
                .withType(content.getType())
                .withTitle(content.getTitle())
                .withPublishedDate(content.getPublishedDate())
                .withByline(content.getByline())
                .withDescription(content.getDescription())
                .withIdentifiers(content.getIdentifiers())
                .withBodyXml(transformedBody)
                .withOpeningXml(transformedOpening)
                .withBinaryUrl(content.getBinaryUrl())
                .withMembers(content.getMembers())
                .withRequestUrl(content.getRequestUrl())
                .withMainImage(content.getMainImage())
                .withComments(content.getComments())
                .withBrands(content.getBrands())
                .withRealtime(content.isRealtime())
                .withPublishReference(content.getPublishReference())
                .withPixelWidth(content.getPixelWidth())
                .withCopyright(content.getCopyright())
                .withPixelHeight(content.getPixelHeight())
                .withLastModifiedDate(content.getLastModified())
                .withStandout(content.getStandout())
                .withStandfirst(content.getStandfirst())
                .withAlternativeTitles(content.getAlternativeTitles())
                .build();
    }
}
