package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Wrapper for future legal stakeholder rights information and the agreed attribution statement / notice.
 *
 * @author Simon
 */
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

}