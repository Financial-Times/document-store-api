package com.ft.universalpublishing.documentstore.health;

import com.codahale.metrics.health.HealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.ft.platform.dropwizard.GoodToGoChecker;
import com.ft.platform.dropwizard.GoodToGoResult;
import com.ft.platform.dropwizard.HealthChecks;

import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DocumentStoreConnectionGoodToGoChecker implements GoodToGoChecker {
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 3;
    private final ExecutorService executor;
    private int timeOutInSeconds;
    
    public DocumentStoreConnectionGoodToGoChecker() {
        this.timeOutInSeconds = DEFAULT_TIMEOUT_IN_SECONDS;
        this.executor = Executors.newFixedThreadPool(3);
    }

    public GoodToGoResult runCheck(Environment environment) {
        try {
            return executor.submit(() -> {
                for (HealthCheck hc : HealthChecks.extractHealthChecksFrom(environment).values()) {
                    if (hc instanceof DocumentStoreConnectionHealthCheck) {
                        AdvancedResult r = ((DocumentStoreConnectionHealthCheck) hc).executeAdvanced();
                        if (r.status() == AdvancedResult.Status.ERROR) {
                            return new GoodToGoResult(false, "Healthcheck \"" + r.getAdvancedHealthCheck().getName() + "\" failed. See /__health for more information.");
                        }
                    }
                }
                return new GoodToGoResult(true, "OK");
            }).get(this.timeOutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new GoodToGoResult(false, "Timed out after " + this.timeOutInSeconds + " second(s)");
        }
    }
}
