package com.ft.universalpublishing.documentstore.validators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.ListItem;
import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ContentListValidatorTest {
    private ContentListValidator contentListValidator = new ContentListValidator(new UuidValidator());
    private ContentList.Builder builder = new ContentList.Builder();
    private String uuid;

    @BeforeEach
    public void setup() {
        uuid = UUID.randomUUID().toString();
        String contentUuid1 = UUID.randomUUID().toString();
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setWebUrl("weburl");
        List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);
        String publishReference = "tid_zxcv7531";

        builder.withUuid(UUID.fromString(uuid)).withTitle("headline").withItems(content)
                .withPublishReference(publishReference);
    }

    @Test
    public void shouldPassIfItemsListIsEmpty() {

    }

    @Test
    public void shouldPassIfPublishReferenceIsNull() {
        ContentList contentList = builder.withPublishReference(null).build();

        contentListValidator.validate(uuid, contentList);
    }

    @Test
    public void shouldFailValidationIfContentListIsNull() {
        Exception exception = assertThrows(ValidationException.class, () -> contentListValidator.validate(uuid, null));

        String expectedMessage = "list must be provided in request body";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfUuidIsNull() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            ContentList contentList = builder.withUuid(null).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "submitted list must provide a non-empty uuid";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfTitleIsNull() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            ContentList contentList = builder.withTitle(null).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "submitted list must provide a non-empty title";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfTitleIsEmpty() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            ContentList contentList = builder.withTitle("").build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "submitted list must provide a non-empty title";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfItemsIsNull() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            ContentList contentList = builder.withItems(null).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "submitted list should have an 'items' field";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfConceptSuppliedButItsPrefLabelIsNull() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            Concept concept = new Concept(UUID.fromString(uuid), null);
            ContentList contentList = builder.withConcept(concept).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "if a concept is supplied it must have a non-empty prefLabel field";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfItemsHaveNeitherUuidOrWebUrl() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            ListItem listItemWithInvalidUuid = new ListItem();
            listItemWithInvalidUuid.setUuid("invalid");
            List<ListItem> contentItems = ImmutableList.of(listItemWithInvalidUuid);
            ContentList contentList = builder.withItems(contentItems).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "invalid uuid: invalid, does not conform to RFC 4122";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfItemsHaveInvalidUuid() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            List<ListItem> contentItems = ImmutableList.of(new ListItem());
            ContentList contentList = builder.withItems(contentItems).build();
            contentListValidator.validate(uuid, contentList);
        });

        String expectedMessage = "list items must have a non-empty uuid or a non-empty webUrl";
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void shouldFailValidationIfUuidOnContentDoesNotMatchUuid() {
        String mismatchedUuid = UUID.randomUUID().toString();
        Exception exception = assertThrows(ValidationException.class, () -> {
            contentListValidator.validate(mismatchedUuid, builder.build());
        });

        String expectedMessage = String.format("uuid in path %s is not equal to uuid in submitted list %s",
                mismatchedUuid, uuid);
        assertThat(exception.getMessage(), equalTo(expectedMessage));
    }
}
