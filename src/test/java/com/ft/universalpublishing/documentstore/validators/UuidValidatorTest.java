package com.ft.universalpublishing.documentstore.validators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import org.junit.jupiter.api.Test;

public class UuidValidatorTest {
  private final UuidValidator validator = new UuidValidator();

  @Test
  public void shouldNotThrowExceptionWhenUuidIsValid() {
    String validUuid = "3c99c2ba-a6ae-11e2-95b1-00144feabdc0";
    validator.validate(validUuid, "uuid");
    assertTrue("Valid UUID was not accepted", true);
  }

  @Test
  public void shouldThrowValidationExceptionWhenUuidIsInvalid() {
    String invalidUuid = "3c99c2ba-a6ae-11e2-95b1-00144feabxxx";
    String fieldName = "uuid";
    Exception exception =
        assertThrows(
            ValidationException.class,
            () -> {
              validator.validate(invalidUuid, fieldName);
            });

    String expectedMessage =
        String.format("invalid %s: %s, does not conform to RFC 4122", fieldName, invalidUuid);
    assertThat(exception.getMessage(), equalTo(expectedMessage));
  }
}
