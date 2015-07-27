package com.ft.universalpublishing.documentstore.model.transformer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.validation.constraints.NotNull;

public class Member implements Comparable<Member> {

    private final String uuid;

    public Member(@JsonProperty("uuid") String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Member uuid must not be null.");
        }
        this.uuid = uuid;
    }

    @NotNull
    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Member other = (Member) obj;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("uuid", uuid)
                .toString();
    }

    @Override
    public int compareTo(Member member) {
        return uuid.compareTo(member.uuid);
    }
}
