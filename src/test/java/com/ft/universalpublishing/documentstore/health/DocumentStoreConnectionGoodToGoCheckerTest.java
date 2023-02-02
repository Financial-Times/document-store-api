package com.ft.universalpublishing.documentstore.health;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.ft.platform.dropwizard.GoodToGoResult;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DocumentStoreConnectionGoodToGoCheckerTest {
  @Mock private Environment env;
  @Mock private MongoDocumentStoreService service;

  private HealthCheckRegistry registry = new HealthCheckRegistry();

  private HealthCheck documentStoreConnectionHealthCheck() {
    HealthcheckParameters healthcheckParameters =
        new HealthcheckParameters(
            "Connectivity to DocumentDB",
            1,
            "business impact message",
            "technical summary message",
            "https://panic_guide_url");
    return new DocumentStoreConnectionHealthCheck(service, healthcheckParameters);
  }

  private HealthCheck documentStoreIndexHealthCheck() {
    HealthcheckParameters healthcheckParameters =
        new HealthcheckParameters(
            "DocumentDB index",
            1,
            "business impact message",
            "technical summary message",
            "https://panic_guide_url");
    return new DocumentStoreIndexHealthCheck(service, healthcheckParameters);
  }

  @BeforeEach
  public void setUp() {
    when(env.healthChecks()).thenReturn(registry);
  }

  @Test
  public void thatIsGoodToGoIfConnectionIsHealthy() {
    when(service.isConnected()).thenReturn(true);
    registry.register("DocumentStoreConnection", documentStoreConnectionHealthCheck());

    DocumentStoreConnectionGoodToGoChecker checker = new DocumentStoreConnectionGoodToGoChecker();
    GoodToGoResult actual = checker.runCheck(env);

    assertTrue(actual.isGoodToGo());
  }

  @Test
  public void thatNotGoodToGoIfConnectionIsUnhealthy() {
    when(service.isConnected()).thenReturn(false);
    registry.register("DocumentStoreConnection", documentStoreConnectionHealthCheck());

    DocumentStoreConnectionGoodToGoChecker checker = new DocumentStoreConnectionGoodToGoChecker();
    GoodToGoResult actual = checker.runCheck(env);

    assertFalse(actual.isGoodToGo());
  }

  @Test
  public void thatOtherHealthChecksAreIgnored() {
    when(service.isConnected()).thenReturn(true);
    registry.register("DocumentStoreConnection", documentStoreConnectionHealthCheck());
    registry.register("DocumentStoreIndex", documentStoreIndexHealthCheck());

    DocumentStoreConnectionGoodToGoChecker checker = new DocumentStoreConnectionGoodToGoChecker();
    GoodToGoResult actual = checker.runCheck(env);

    assertTrue(actual.isGoodToGo());
  }
}
