package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Standout;

public class StandoutMapper {

    public com.ft.universalpublishing.documentstore.model.read.Standout map(final Standout source) {
        if (source == null) {
            return null;
        }
        return new com.ft.universalpublishing.documentstore.model.read.Standout(
                source.isEditorsChoice(),
                source.isExclusive(),
                source.isScoop()
        );
    }
}
