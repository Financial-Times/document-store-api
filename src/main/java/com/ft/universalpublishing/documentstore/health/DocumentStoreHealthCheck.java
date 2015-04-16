package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.DB;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentStoreHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStoreHealthCheck.class);

    private final DB db;
    private HealthcheckParameters healthcheckParameters;

    public DocumentStoreHealthCheck(DB db, HealthcheckParameters healthcheckParameters) {
        super(healthcheckParameters.getName());

        this.db = db;
        this.healthcheckParameters = healthcheckParameters;
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

            if (mongodbIsAvailable()) {
                return AdvancedResult.healthy("OK");
            } else {
                return AdvancedResult.error(this, "Error occurred opening the socket, connection refused");
            }
    }

    private boolean mongodbIsAvailable() {
        try {
            db.command("serverStatus");
        } catch (MongoException e){
            LOGGER.warn("Unable to get connection to Mongo repository.", e);
            return false;
        }
        return true;
    }

    @Override
    protected int severity() { return healthcheckParameters.getSeverity(); }

    @Override
    protected String businessImpact() { return healthcheckParameters.getBusinessImpact(); }

    @Override
    protected String technicalSummary() { return healthcheckParameters.getTechnicalSummary(); }

    @Override
    protected String panicGuideUrl() { return healthcheckParameters.getPanicGuideUrl(); }

}
