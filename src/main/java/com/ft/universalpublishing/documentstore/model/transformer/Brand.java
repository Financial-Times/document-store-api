package com.ft.universalpublishing.documentstore.model.transformer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Brand implements Comparable<Brand> {

    private String id;

    public Brand(@JsonProperty("id") String id) {
        if (id == null) {
            throw new IllegalArgumentException("Brand id must not be null.");
        }
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return id.equals(brand.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Brand brand) {
        return getId().compareTo(brand.getId());
    }
}
