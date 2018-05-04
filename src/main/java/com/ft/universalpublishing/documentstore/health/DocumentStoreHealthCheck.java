package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentStoreHealthCheck extends AdvancedHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStoreHealthCheck.class);

    private final MongoDatabase db;
    private final HealthcheckParameters healthcheckParameters;


    public DocumentStoreHealthCheck(MongoDatabase db, HealthcheckParameters healthcheckParameters) {
        super(healthcheckParameters.getName());
        this.db = db;
        this.healthcheckParameters = healthcheckParameters;
    }

    @Override
    protected AdvancedResult checkAdvanced() {

        final String message = "Cannot connect to MongoDB";

        try {
            Document commandResult = db.runCommand(Document.parse("{ serverStatus : 1 }"));
            boolean isOK = !commandResult.isEmpty();

            if (isOK) {
                return AdvancedResult.healthy("OK");
            }
        }
        catch (MongoException e) {
            LOGGER.warn(message, e);
            return AdvancedResult.error(this, e);
        }
        return AdvancedResult.error(this, message);
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
