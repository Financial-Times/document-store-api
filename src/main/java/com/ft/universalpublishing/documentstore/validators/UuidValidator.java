package com.ft.universalpublishing.documentstore.validators;

import java.util.UUID;

import com.ft.universalpublishing.documentstore.exception.ValidationException;

public class UuidValidator {

    public void validate(String uuid, String fieldName) {
        try {
            final UUID parsedUuid = UUID.fromString(uuid);
            if (!parsedUuid.toString().equals(uuid)) {
                throw new ValidationException(
                        String.format("invalid %s: " + uuid + ", does not conform to RFC 4122", fieldName));
            }
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new ValidationException(
                    String.format("invalid %s: " + uuid + ", does not conform to RFC 4122", fieldName));
        }
    }
}
