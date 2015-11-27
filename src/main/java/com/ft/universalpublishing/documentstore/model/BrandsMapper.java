package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Brand;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BrandsMapper {

    public SortedSet<String> map(final SortedSet<Brand> source) {
        if (source == null) {
            return null;
        }
        SortedSet<String> target = new TreeSet<>();
        target.addAll(source.stream()
                .map(Brand::getId)
                .collect(Collectors.toCollection(TreeSet::new)));
        return target;
    }
}
