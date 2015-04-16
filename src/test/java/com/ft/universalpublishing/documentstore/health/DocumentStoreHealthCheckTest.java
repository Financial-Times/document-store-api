package com.ft.universalpublishing.documentstore.health;

import com.ft.universalpublishing.documentstore.MongoConfig;
import com.mongodb.DB;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;


public class DocumentStoreHealthCheckTest {

    private DocumentStoreHealthCheck healthCheck;
    private HealthcheckParameters healthcheckParameters;

    @Mock private DB db;
    @Mock private MongoConfig mockMongoConfig;

    @Before
    public void setUp() {
        healthcheckParameters = new HealthcheckParameters("Connectivity to MongoDB", 1, "business impact message",
                "technical summary message", "https://panic_guide_url");
        when(mockMongoConfig.getHost()).thenReturn("localhost");
        when(mockMongoConfig.getPort()).thenReturn(14180);
        healthCheck = new DocumentStoreHealthCheck(db, healthcheckParameters);
    }

    @Test
    public void shouldReturnHealthyWhenConnectionToMongoDBIsSuccessful() throws Exception {

    }

    @Test
    public void shouldReturnUnhealthyWhenConnectionToMongoDBIsUnsuccessful() throws Exception {

    }
}
