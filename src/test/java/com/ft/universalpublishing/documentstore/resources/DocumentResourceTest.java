package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.model.BrandsMapper;
import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.IdentifierMapper;
import com.ft.universalpublishing.documentstore.model.TypeResolver;
import com.ft.universalpublishing.documentstore.model.read.Content;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.transform.ContentBodyProcessingService;
import com.ft.universalpublishing.documentstore.transform.ModelBodyXmlTransformer;
import com.ft.universalpublishing.documentstore.transform.UriMerger;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {

    @Test
    public void test() {
        final DocumentStoreService mockStore = mock(DocumentStoreService.class);
        final ContentListValidator mockValidator = mock(ContentListValidator.class);
        final ContentMapper contentMapper = new ContentMapper(new IdentifierMapper(), new TypeResolver(), new BrandsMapper(), "localhost");
        final Map<String, String> templates = new HashMap<>();
        templates.put("http://www.ft.com/ontology/content/Article", "/content/{{id}}");
        templates.put("http://www.ft.com/ontology/content/ImageSet", "/content/{{id}}");
        final ContentBodyProcessingService bodyProcessingService = new ContentBodyProcessingService(
                new ModelBodyXmlTransformer(
                        new UriMerger(templates)
                )
        );
        final ApiUriGenerator mockedUriGenerator = mock(ApiUriGenerator.class);
        final DocumentResource resource = new DocumentResource(mockStore, mockValidator, new UuidValidator(), "api.ft.com/", contentMapper, bodyProcessingService);

        final UUID uuid = UUID.randomUUID();
        final Map<String, Object> content = new HashMap<>();
        content.put("uuid", uuid.toString());
        content.put("title", "Hello!");
        content.put("publishedDate", new Date());
        content.put("mainImage", UUID.randomUUID().toString());
        final Map<String, Object> comments = new HashMap<>();
        comments.put("enabled", true);
        content.put("comments", comments);
        content.put("publishReference", "Some String");

        when(mockStore.findByUuid(DocumentResource.CONTENT_COLLECTION, uuid)).thenReturn(content);
        Content rContent = resource.getContentReadByUuid(uuid.toString(), mockedUriGenerator);
        assertThat(rContent.getId(),containsString(uuid.toString()));
    }
}
