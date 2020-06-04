package com.ft.universalpublishing.documentstore.health;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DocumentStoreConnectionHealthCheckTest {
  private DocumentStoreConnectionHealthCheck healthcheck;

  @Mock private HealthcheckParameters healthcheckParameters;

  @Mock private MongoDocumentStoreService service;

  @BeforeEach
  public void setUp() {
    healthcheckParameters =
        new HealthcheckParameters(
            "Connectivity to MongoDB",
            1,
            "business impact message",
            "technical summary message",
            "https://panic_guide_url");
    healthcheck = new DocumentStoreConnectionHealthCheck(service, healthcheckParameters);
  }

  @Test
  public void shouldReturnOKStatusWhenConnectedIsTrue() {
    when(service.isConnected()).thenReturn(true);

    AdvancedResult result = healthcheck.checkAdvanced();

    assertThat(result.status(), is(AdvancedResult.Status.OK));
  }

  @Test
  public void shouldReturnErrorStatusWhenConnectedIsFalse() {
    when(service.isConnected()).thenReturn(false);

    AdvancedResult result = healthcheck.checkAdvanced();

    assertThat(result.status(), is(AdvancedResult.Status.ERROR));
  }
}
