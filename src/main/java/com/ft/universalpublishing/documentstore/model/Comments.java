package com.ft.universalpublishing.documentstore.model;

public class Comments {
    private boolean enabled;

    public Comments() {
    }

    public Comments(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comments that = (Comments) o;
        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return (enabled ? 1 : 0);
    }
}
