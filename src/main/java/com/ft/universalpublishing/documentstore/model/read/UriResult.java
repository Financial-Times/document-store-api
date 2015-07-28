package com.ft.universalpublishing.documentstore.model.read;

import com.google.common.collect.ComparisonChain;

public class UriResult implements Comparable<UriResult> {
    private String id;

    public UriResult(String id) {
        this.id = id;
    }

    /**
     * Needed for Jackson
     */
    public UriResult() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriResult uriResult = (UriResult) o;

        return !(id != null ? !id.equals(uriResult.id) : uriResult.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(UriResult that) {
        return ComparisonChain.start().compare(this.id, that.id).result();
    }
}
