package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Brand;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BrandsMapper {

    private static final String FT_BRAND = "http://api.ft.com/things/dbb0bdae-1f0c-11e4-b0cb-b2227cce2b54";

    public SortedSet<String> map(final SortedSet<Brand> source) {
        SortedSet<String> target = new TreeSet<>();
        target.add(FT_BRAND);
        if (source != null) {
            target.addAll(source.stream()
                    .map(Brand::getId)
                    .collect(Collectors.toCollection(TreeSet::new)));
        }
        return target;
    }
}
