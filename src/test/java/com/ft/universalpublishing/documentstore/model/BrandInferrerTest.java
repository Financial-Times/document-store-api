package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.read.Content;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class BrandInferrerTest {

    final BrandInferrer brandInferrer = new BrandInferrer();

    @Test
    public void testInferNonEmptyBrands() throws Exception {
        final SortedSet<String> brands = new TreeSet<>();
        brands.add("ft.com/things/something-from-before");
        final Content source = new Content.Builder()
                .withTitle("Title")
                .withBrands(brands)
                .build();

        final Content target = brandInferrer.infer(source);

        assertThat(target.getTitle(), equalTo("Title"));
        assertThat(target.getBrands(), hasItem("ft.com/things/something-from-before"));
        assertThat(target.getBrands(), hasItem("http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54"));
    }

    @Test
    public void testInferEmptyBrands() throws Exception {
        final Content source = new Content.Builder()
                .withTitle("Title")
                .build();

        final Content target = brandInferrer.infer(source);

        assertThat(target.getTitle(), equalTo("Title"));
        assertThat(target.getBrands(), hasItem("http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54"));
    }
}