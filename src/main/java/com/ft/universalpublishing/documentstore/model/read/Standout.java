package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Standout {
    private final boolean editorsChoice;
    private final boolean exclusive;
    private final boolean scoop;

    public Standout(@JsonProperty("editorsChoice") boolean editorsChoice,
                    @JsonProperty("exclusive") boolean exclusive,
                    @JsonProperty("scoop") boolean scoop) {
        this.editorsChoice = editorsChoice;
        this.scoop = scoop;
        this.exclusive = exclusive;
    }

    public boolean isEditorsChoice() {
        return editorsChoice;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public boolean isScoop() {
        return scoop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Standout standout = (Standout) o;

        if (editorsChoice != standout.editorsChoice) return false;
        if (scoop != standout.scoop) return false;
        return exclusive == standout.exclusive;

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(editorsChoice, exclusive, scoop);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("editorsChoice", editorsChoice)
                .add("exclusive", exclusive)
                .add("scoop", scoop)
                .toString();
    }
}
