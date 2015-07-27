package com.ft.universalpublishing.documentstore.model.transformer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Item {

    private String uuid;

    public Item(String uuid) {
        this.uuid = uuid;
    }

    public Item(String uuid, String type) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item that = (Item) o;
        return Objects.equal(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .toString();
    }
}
