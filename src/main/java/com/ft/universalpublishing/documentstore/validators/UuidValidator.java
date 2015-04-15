package com.ft.universalpublishing.documentstore.validators;

import java.util.UUID;

import com.ft.universalpublishing.documentstore.exception.ValidationException;

public class UuidValidator {

    public void validate(String uuid) {
        try {
            final UUID parsedUuid = UUID.fromString(uuid);
            if (!parsedUuid.toString().equals(uuid)) {
                throw new ValidationException("invalid UUID: " + uuid + ", does not conform to RFC 4122");
            }
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("invalid UUID: " + uuid + ", does not conform to RFC 4122");
        }
    }

}
