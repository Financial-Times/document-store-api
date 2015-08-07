package com.ft.universalpublishing.documentstore.model;

import com.ft.universalpublishing.documentstore.model.transformer.Identifier;

public class IdentifierMapper {

    public com.ft.universalpublishing.documentstore.model.read.Identifier map(Identifier identifier) {
        return new com.ft.universalpublishing.documentstore.model.read.Identifier(identifier.getAuthority(),
                identifier.getIdentifierValue()
        );
    }
}
