package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.IdentifierMapper;
import com.ft.universalpublishing.documentstore.model.TypeResolver;
import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {

    @Test
    public void test() {
        final DocumentStoreService mockStore = mock(DocumentStoreService.class);
        final ContentListValidator mockValidator = mock(ContentListValidator.class);
        final ContentMapper contentMapper = new ContentMapper(new IdentifierMapper(), new TypeResolver(), "localhost");
        final DocumentResource resource = new DocumentResource(mockStore, mockValidator, new UuidValidator(), "api.ft.com/", contentMapper);

        final UUID uuid = UUID.randomUUID();
        final Map<String, Object> content = new HashMap<>();
        content.put("uuid", uuid.toString());
        content.put("title", "Hello!");
        content.put("publishedDate", new Date());
        content.put("mainImage", UUID.randomUUID().toString());
        final Map<String, Object> comments = new HashMap<>();
        comments.put("enabled", true);
        content.put("comments", comments);

        when(mockStore.findByUuid(DocumentResource.CONTENT_COLLECTION, uuid)).thenReturn(content);
        Content rContent = resource.getContentReadByUuid(uuid.toString());
        rContent.getId();
    }
}
