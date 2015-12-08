package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DocumentProcessingContextTest {

    @Mock
    ApiUriGenerator generator;

    @Test
    public void shouldReportTagIsBeingProcessedIfSomethingReportedIt() {
        DocumentProcessingContext sit = new DocumentProcessingContext(generator);
        sit.processingStarted("tag");
        assertThat(sit.isProcessing("tag"), is(true));
    }

    @Test
    public void shouldNotReportTagIsBeingProcessedIfNothingReportedIt_EvenIfItIsBeingProcessed() {
        DocumentProcessingContext sit = new DocumentProcessingContext(generator);
        assertThat(sit.isProcessing("tag"), is(false));
    }

    @Test
    public void shouldNotReportProcessingAfterItStops() {
        DocumentProcessingContext sit = new DocumentProcessingContext(generator);
        sit.processingStarted("tag");
        sit.processingStopped("tag");
        assertThat(sit.isProcessing("tag"), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfSomethingReportsTheEndOfProcessingThatDidNotStart() {
        DocumentProcessingContext sit = new DocumentProcessingContext(generator);
        sit.processingStopped("tag");
    }

}
