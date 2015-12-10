package com.ft.universalpublishing.documentstore.transform;

import com.ft.bodyprocessing.BodyProcessingContext;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;

import java.util.Set;
import java.util.TreeSet;

public class DocumentProcessingContext implements BodyProcessingContext {

    private Set<String> processing;
    private ApiUriGenerator uriGenerator;

    public DocumentProcessingContext(ApiUriGenerator uriGenerator) {
        this.uriGenerator = uriGenerator;
        this.processing = new TreeSet<>();
    }

    public ApiUriGenerator getUriGenerator() {
        return uriGenerator;
    }

    public boolean isProcessing(String tagName) {
        return processing.contains(tagName);
    }

    public void processingStarted(String tagName) {
        processing.add(tagName);
    }

    public void processingStopped(String tagName) {
        if(!processing.remove(tagName)) {
            throw new IllegalArgumentException(tagName + " isn't being processed");
        }
    }


}
