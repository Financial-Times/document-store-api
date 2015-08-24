package com.ft.universalpublishing.documentstore.transform;

import com.ft.universalpublishing.documentstore.util.FixedUriGenerator;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.fail;

public class BodyTransformationStepDefs {

	private final ThreadLocal<Map<String, String>> mapping = new ThreadLocal<>();
	private final ThreadLocal<String> body = new ThreadLocal<>();

	@Given("^I have a url template mapping of type (.+) to url (.+)$")
	public void haveUrlMapping(final String type, final String template) {
		if (mapping.get() == null) {
			mapping.set(new HashMap<>());
		}
		mapping.get().put(type, template);
	}

	@Given("^I have body text in the article consisting of (.+)$")
	public void haveBodyContent(final String body) {
        this.body.set(body);
    }

	@When("^I transform the article for output$")
	public void transform() {
		body.set(new ModelBodyXmlTransformer(new UriBuilder(mapping.get())).transform(body.get(), localProcessingContext()));
	}

    private ContentBodyProcessingContext localProcessingContext() {
        return new ContentBodyProcessingContext(FixedUriGenerator.localUriGenerator() );
    }

    @When("I transform the article for output it will fail")
	public void shouldFailToTransform() {
		try {
			transform();
			fail("Transformation completed without failure");
		} catch (final BodyTransformationException e) {
			// OK. Expected exception.
		}
	}

	@Then("^the body text in the article should have been transformed into (.+)$")
	public void shouldHaveBeenTransformedInto(final String expected) {
		assertXMLEqual(xmlDocument(expected), xmlDocument(body.get()));
	}

	private static Document xmlDocument(final String xml) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			return documentBuilder.parse(new InputSource(new StringReader(xml)));
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException("Couldn't make document builder", e);
		} catch (final Exception e) {
			throw new AssertionError("Couldn't parse XML", e);
		}
	}
}
