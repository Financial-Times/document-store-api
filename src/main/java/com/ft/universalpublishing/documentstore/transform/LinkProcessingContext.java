package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;

public class LinkProcessingContext implements BodyProcessingContext {

    private boolean processingContent = false;
    private ApiUriGenerator uriGenerator;

    public LinkProcessingContext(ApiUriGenerator uriGenerator) {
        this.uriGenerator = uriGenerator;
    }

    public ApiUriGenerator getUriGenerator() {
        return uriGenerator;
    }

    public boolean isProcessingLink() {
        return this.processingContent;
    }

    public void setProcessingLink(final boolean processingContent) {
        this.processingContent = processingContent;
    }

}