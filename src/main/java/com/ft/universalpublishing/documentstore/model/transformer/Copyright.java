package com.ft.universalpublishing.documentstore.model.transformer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * Wrapper for future legal stakeholder rights information and the agreed attribution statement / notice.
 *
 * @author Simon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Copyright {

    private String notice;

    public Copyright(@JsonProperty("notice") String notice) {
        this.notice = notice;
    }

    public String getNotice() {
        return notice;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Copyright that = (Copyright) o;

        return Objects.equals(this.notice, that.notice);	}

    @Override
    public int hashCode() {
        return Objects.hashCode(notice);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("notice", notice)
                .toString();
    }
}