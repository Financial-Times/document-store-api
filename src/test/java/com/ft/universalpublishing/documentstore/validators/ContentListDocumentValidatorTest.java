package com.ft.universalpublishing.documentstore.validators;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.ListItem;
import com.google.common.collect.ImmutableList;


public class ContentListDocumentValidatorTest {

    private ContentListDocumentValidator contentListDocumentValidator = new ContentListDocumentValidator(new UuidValidator());
    private ContentList.Builder builder = new ContentList.Builder();
    private String uuid;
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setup() {
        uuid = UUID.randomUUID().toString();
        String contentUuid1 = UUID.randomUUID().toString();
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setWebUrl("weburl");
        List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);
        
        builder.withUuid(UUID.fromString(uuid))
               .withTitle("headline")
               .withItems(content);
    }
    
    @Test
    public void shouldPassIfItemsListIsEmpty() {
        
    }
    
    @Test
    public void shouldFailValidationIfContentListIsNull() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("list must be provided in request body");
        
        contentListDocumentValidator.validate(uuid, null);
    }
    
    @Test
    public void shouldFailValidationIfUuidIsNull() {
        ContentList contentList = builder.withUuid(null).build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted list must provide a non-empty uuid");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfTitleIsNull() {
        ContentList contentList = builder.withTitle(null).build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted list must provide a non-empty title");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfTitleIsEmpty() {
        ContentList contentList = builder.withTitle("").build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted list must provide a non-empty title");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfItemsIsNull() {
        ContentList contentList = builder.withItems(null).build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("submitted list should have an 'items' field");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfItemsHaveNeitherUuidOrWebUrl() {
        ListItem listItemWithInvalidUuid = new ListItem();
        listItemWithInvalidUuid.setUuid("invalid");
        List<ListItem> contentItems = ImmutableList.of(listItemWithInvalidUuid);
        ContentList contentList = builder.withItems(contentItems).build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("invalid UUID: invalid, does not conform to RFC 4122");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfItemsHaveInvalidUuid() {
        List<ListItem> contentItems = ImmutableList.of(new ListItem());
        ContentList contentList = builder.withItems(contentItems).build();
        
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("list items must have a non-empty uuid or a non-empty webUrl");
        
        contentListDocumentValidator.validate(uuid, contentList);
    }
    
    @Test
    public void shouldFailValidationIfUuidOnContentDoesNotMatchUuid() { 
        String mismatchedUuid = UUID.randomUUID().toString();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("uuid in path %s is not equal to uuid in submitted list %s", mismatchedUuid, uuid));

        contentListDocumentValidator.validate(mismatchedUuid, builder.build());
    }
}
