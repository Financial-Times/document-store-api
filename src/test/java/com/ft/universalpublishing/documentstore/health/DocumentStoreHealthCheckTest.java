package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreHealthCheckTest {
//
//    private DocumentStoreHealthCheck healthcheck;
//
//    @Mock private HealthcheckParameters healthcheckParameters;
//    @Mock private MongoDatabase db;
//    @Mock private CommandResult commandResult;
//
//    @Before
//    public void setUp() {
//        healthcheckParameters = new HealthcheckParameters("Connectivity to MongoDB", 1, "business impact message",
//                "technical summary message", "https://panic_guide_url");
//        healthcheck = new DocumentStoreHealthCheck(db, healthcheckParameters);
//    }
//
//    @Test
//    public void shouldReturnOKStatusWhenCommandResultIsTrue() throws Exception {
//        when(commandResult.ok()).thenReturn(true);
//        when(db.command("serverStatus")).thenReturn(commandResult);
//
//        AdvancedResult result = healthcheck.checkAdvanced();
//
//        assertThat(result.status(), is(AdvancedResult.Status.OK));
//    }
//
//    @Test
//    public void shouldReturnErrorStatusWhenCommandResultIsFalse() throws Exception {
//        when(commandResult.ok()).thenReturn(false);
//        when(db.command("serverStatus")).thenReturn(commandResult);
//
//        AdvancedResult result = healthcheck.checkAdvanced();
//
//        assertThat(result.status(), is(AdvancedResult.Status.ERROR));
//    }
//
//    @Test
//    public void shouldReturnErrorWhenConnectionToMongoDBIsUnsuccessful()  throws Exception {
//        when(db.command(anyString())).thenThrow(MongoException.class);
//
//        AdvancedResult result = healthcheck.checkAdvanced();
//
//        assertThat(result.status(), is(AdvancedResult.Status.ERROR));
//    }
}
