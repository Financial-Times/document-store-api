package com.ft.universalpublishing.documentstore.validators;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import org.junit.Before;
import org.junit.Test;

public class UuidValidatorTest {

    private UuidValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new UuidValidator();
    }

    @Test
    public void shouldNotReturnNothingAsUuidIsValid() throws Exception {
        String uuid = "3c99c2ba-a6ae-11e2-95b1-00144feabdc0";
        validator.validate(uuid);
        assertTrue("Valid UUID was not accepted", true);
    }

    @Test
    public void shouldThrowValidationExceptionWhenUuidIsInvalid() throws Exception {
        String uuid = "3c99c2ba-a6ae-11e2-95b1-00144feabxxx";
        try{
            validator.validate(uuid);
            fail("Invalid UUID was accepted");
        }
        catch (ValidationException ve){
            assertThat("Message in exception did not match", "invalid UUID: " + uuid + ", does not conform to RFC 4122",
                    equalTo(ve.getMessage()));
        }

    }
}