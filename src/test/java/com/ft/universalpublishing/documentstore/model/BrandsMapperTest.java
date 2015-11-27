package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Brand;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class BrandsMapperTest {

    final BrandsMapper brandsMapper = new BrandsMapper();

    @Test
    public void testInferNonEmptyBrands() throws Exception {
        final SortedSet<Brand> brands = new TreeSet<>();
        brands.add(new Brand("http://api.ft.com/things/something-from-before"));

        final SortedSet<String> target = brandsMapper.map(brands);

        assertThat(target, hasItem("http://api.ft.com/things/something-from-before"));
        assertThat(target, not(hasItem("http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54")));
    }

    @Test
    public void testInferEmptyBrands() throws Exception {
        final SortedSet<String> target = brandsMapper.map(null);

        assertThat(target, nullValue());
    }
}
