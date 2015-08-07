package com.ft.universalpublishing.documentstore.model.read;

import com.google.common.collect.ComparisonChain;

public class Uri implements Comparable<Uri> {
    private String id;

    public Uri(String id) {
        this.id = id;
    }

    public Uri() {
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
        Uri uri = (Uri) o;
        return !(id != null ? !id.equals(uri.id) : uri.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Uri that) {
        return ComparisonChain.start().compare(this.id, that.id).result();
    }
}
