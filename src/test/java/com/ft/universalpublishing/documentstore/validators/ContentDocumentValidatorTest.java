package com.ft.universalpublishing.documentstore.validators;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Content;


public class ContentDocumentValidatorTest {

    private ContentDocumentValidator contentDocumentValidator = new ContentDocumentValidator();
    private Content content;
    private String uuid;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    
    @Before
    public void setup() {
        uuid = UUID.randomUUID().toString();
        Date lastPublicationDate = new Date();
        content = new Content();
        content.setUuid(uuid);
        content.setTitle("Here's the news");
        content.setBodyXml("xmlBody");
        content.setPublishedDate(lastPublicationDate);
    }
    
    @Test
    public void shouldPassIfBylineIsNull() {
        content.setByline(null);
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldPassIfBylineIsEmpty() {
        content.setByline("");
        contentDocumentValidator.validate(uuid, content);
        
    }
    
    @Test
    public void shouldPassIfBodyXmlIsNull() {
        content.setBodyXml(null);
        contentDocumentValidator.validate(uuid, content);
        
    }
    
    @Test
    public void shouldPassIfBodyXmlIsEmpty() {
        content.setBodyXml(null);
        contentDocumentValidator.validate(uuid, content);
        
    }
    
    @Test
    public void shouldFailValidationIfContentIsNull() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("content must be provided in request body");
        
        contentDocumentValidator.validate(uuid, null);
    }
    
    @Test
    public void shouldFailValidationIfUuidIsNull() {
        content.setUuid(null);
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted content must provide a non-empty uuid");
        
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldFailValidationIfUuidIsEmpty() {
        content.setUuid("");
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted content must provide a non-empty uuid");
        
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldFailValidationIfTitleIsNull() {
        content.setTitle(null);
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted content must provide a non-empty title");
        
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldFailValidationIfTitleIsEmpty() {
        content.setTitle("");
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted content must provide a non-empty title");
        
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldFailValidationIfPublishedDateIsNull() {
        content.setPublishedDate(null);
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted content must provide a non-empty publishedDate");
        
        contentDocumentValidator.validate(uuid, content);
    }
    
    @Test
    public void shouldFailValidationIfUuidOnContentDoesNotMatchUuid() { 
        String mismatchedUuid = UUID.randomUUID().toString();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("uuid in path %s is not equal to uuid in submitted content %s", mismatchedUuid, uuid));

        contentDocumentValidator.validate(mismatchedUuid, content);
    }
    
    
}
