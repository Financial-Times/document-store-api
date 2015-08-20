package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.util.FixedUriGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelBodyXmlTransformerTest {

	private ModelBodyXmlTransformer unit;
    private ContentBodyProcessingContext mockProcessingContext;

	@Before
	public void before() {
		final Map<String, String> contentTypeTemplates =
                Collections.singletonMap("http://www.ft.com/ontology/content/Article", "http://localhost:9090/content/{{id}}");
        unit = new ModelBodyXmlTransformer(new UriMerger(contentTypeTemplates));
        mockProcessingContext = new ContentBodyProcessingContext(FixedUriGenerator.localUriGenerator());
	}

	@Test
	public void testNormalModel() throws Exception {
		Content original = new Content.Builder()
                .withBodyXml("<xml/>")
                .build();

		Content actual = unit.transform(original, mockProcessingContext);

		assertThat(actual.getBodyXML(), equalTo("<xml></xml>"));
	}

    @Test
    public void shouldEscapeLessThanBracket() throws Exception {
        Content original = new Content.Builder()
                .withBodyXml("<xml>&lt;i&gt;</xml>")
                .build();

        Content actual = unit.transform(original, mockProcessingContext);

        assertThat(actual.getBodyXML(), equalTo("<xml>&lt;i></xml>"));
    }
}
