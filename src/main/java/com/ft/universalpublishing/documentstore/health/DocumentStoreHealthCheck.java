package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentStoreHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStoreHealthCheck.class);

    private DB db;
    private HealthcheckParameters healthcheckParameters;


    public DocumentStoreHealthCheck(DB db, HealthcheckParameters healthcheckParameters) {
        super(healthcheckParameters.getName());
        this.db = db;
        this.healthcheckParameters = healthcheckParameters;

    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

        CommandResult result;

        try {

            result = db.command("serverStatus");
            boolean isOK = result.ok();

            if (isOK) {
                return AdvancedResult.healthy("OK");
            }

        }
        catch (MongoException e) {
            final String message = "Cannot connect to MongoDB";
            LOGGER.warn(message, e);
            return AdvancedResult.error(this, e);
        }
        return AdvancedResult.error(this, "Unable to connect to MongoDB");
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
