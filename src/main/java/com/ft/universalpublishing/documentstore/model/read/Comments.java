package com.ft.universalpublishing.documentstore.model.read;

public class Comments {
    private boolean enabled;

    /**
     * Needed for Jackson
     */
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

        CommentsResult that = (CommentsResult) o;

        return enabled == that.enabled;

    }

    @Override
    public int hashCode() {
        return (enabled ? 1 : 0);
    }
}
