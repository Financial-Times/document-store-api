package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.ListItem;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class UuidValidatorTest {

    private final UuidValidator validator = new UuidValidator();

    @Test
    public void shouldNotThrowExceptionWhenUuidIsValid() {
        String validUuid = "3c99c2ba-a6ae-11e2-95b1-00144feabdc0";
        validator.validate(validUuid);
        assertTrue("Valid UUID was not accepted", true);
    }

    @Test
    public void shouldThrowValidationExceptionWhenUuidIsInvalid() {
        String invalidUuid = "3c99c2ba-a6ae-11e2-95b1-00144feabxxx";
        Exception exception = assertThrows(ValidationException.class,
                () -> {
                    validator.validate(invalidUuid);
                });

        String expectedMessage = String.format("invalid UUID: %s, does not conform to RFC 4122", invalidUuid);
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }
}