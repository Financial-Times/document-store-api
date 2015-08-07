package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreHealthCheckTest {

    private DocumentStoreHealthCheck healthcheck;

    @Mock
    private HealthcheckParameters healthcheckParameters;
    @Mock
    private MongoDatabase db;
    @Mock
    private Document commandResult;

    @Before
    public void setUp() {
        healthcheckParameters = new HealthcheckParameters("Connectivity to MongoDB", 1, "business impact message",
                "technical summary message", "https://panic_guide_url");
        healthcheck = new DocumentStoreHealthCheck(db, healthcheckParameters);
    }

    @Test
    public void shouldReturnOKStatusWhenCommandResultIsTrue() throws Exception {
        when(commandResult.isEmpty()).thenReturn(false);
        when(db.runCommand(Document.parse("{ serverStatus : 1 }"))).thenReturn(commandResult);

        AdvancedResult result = healthcheck.checkAdvanced();

        assertThat(result.status(), is(AdvancedResult.Status.OK));
    }

    @Test
    public void shouldReturnErrorStatusWhenCommandResultIsFalse() throws Exception {
        when(commandResult.isEmpty()).thenReturn(true);
        when(db.runCommand(Document.parse("{ serverStatus : 1 }"))).thenReturn(commandResult);

        AdvancedResult result = healthcheck.checkAdvanced();

        assertThat(result.status(), is(AdvancedResult.Status.ERROR));
    }

    @Test
    public void shouldReturnErrorWhenConnectionToMongoDBIsUnsuccessful() throws Exception {
        when(db.runCommand(any(Document.class))).thenThrow(MongoException.class);

        AdvancedResult result = healthcheck.checkAdvanced();

        assertThat(result.status(), is(AdvancedResult.Status.ERROR));
    }
}
