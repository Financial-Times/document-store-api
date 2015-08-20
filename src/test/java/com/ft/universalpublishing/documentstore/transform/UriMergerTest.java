package com.ft.universalpublishing.documentstore.transform;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UriMergerTest {

    public static final String TYPE_A = "http://example.com/ontlogy/types/A";
    public static final String TYPE_B = "http://example.com/ontlogy/types/B";
    public static final String TYPE_C = "http://example.com/ontlogy/types/C";
    private UriMerger mergerUnderTest;

    @Before
    public void givenTemplatesForTypesAAndB() {
        Map<String,String> templates = new HashMap<>(2);
        templates.put(TYPE_A,"http://example.com/ui/A/{{id}}");
        templates.put(TYPE_B,"http://example.com/admin/B/{{id}}/edit");

        assertThat(templates.size(),is(2));
        mergerUnderTest = new UriMerger(templates);
    }

    @Test
    public void shouldProduceTypeALinkUsingTypeATemplate() {
        String result = mergerUnderTest.mergeUrl(TYPE_A,"1");
        assertThat(result,is("http://example.com/ui/A/1"));
    }

    @Test
    public void shouldProduceTypeBLinkUsingTypeBTemplate() {
        String result = mergerUnderTest.mergeUrl(TYPE_B,"976");
        assertThat(result,is("http://example.com/admin/B/976/edit"));
    }

    @Test(expected = BodyTransformationException.class)
    public void shouldThrowPredictedExceptionTypeForUnknownTypeCLink() {
        mergerUnderTest.mergeUrl(TYPE_C,"16991280068");
    }
}
