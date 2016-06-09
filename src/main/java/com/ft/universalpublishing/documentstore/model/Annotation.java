package com.ft.universalpublishing.documentstore.model;

public class Annotation {
    String predicate;
    String uri;
    String apiUrl;
    String label;
    String type;

    public Annotation(String predicate, String uri, String apiUrl, String label, String type) {
        super();
        this.predicate = predicate;
        this.uri = uri;
        this.apiUrl = apiUrl;
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return !(predicate != null ? !predicate.equals(that.predicate) : that.predicate != null) &&
                !(uri != null ? !uri.equals(that.uri) : that.uri != null);
    }

    @Override
    public int hashCode() {
        int result = predicate != null ? predicate.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }
}
