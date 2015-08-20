package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;

public class ContentBodyProcessingContext implements BodyProcessingContext {

    private boolean processingContent = false;
    private ApiUriGenerator uriGenerator;

    public ContentBodyProcessingContext(ApiUriGenerator uriGenerator) {
        this.uriGenerator = uriGenerator;
    }

    public ApiUriGenerator getUriGenerator() {
        return uriGenerator;
    }

    public boolean isProcessingContent() {
        return this.processingContent;
    }

    public void setProcessingContent(final boolean processingContent) {
        this.processingContent = processingContent;
    }

}
