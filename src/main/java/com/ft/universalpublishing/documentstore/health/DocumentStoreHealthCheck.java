package com.ft.universalpublishing.documentstore.health;

import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;
import com.mongodb.DB;
import com.mongodb.MongoException;
import org.slf4j.LoggerFactory;

public class DocumentStoreHealthCheck extends AdvancedHealthCheck {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DocumentStoreHealthCheck.class);
    private final DB db;

    public DocumentStoreHealthCheck(DB db) {
        super("mongodb");
        this.db = db;
    }
    @Override
    protected AdvancedResult checkAdvanced() throws Exception {

            if (mongodbIsAvailable()) {
                return AdvancedResult.healthy("OK");
            } else {
                return AdvancedResult.error(this, "Failed to connect");
            }
    }

    private boolean mongodbIsAvailable() {
        try {
            db.getLastError(); // TODO - was noted that there is probably a better way to do this
        } catch (MongoException e){
            LOGGER.error("Unable to get connection to Mongo repository.", e);
            return false;
        }
        return true;
    }

    @Override
    protected int severity() { return 2; }

    @Override
    protected String businessImpact() { return "Content will not be accessible or storable"; }

    @Override
    protected String technicalSummary() { return "Cannot connect to the MongoDB content store."; }

    @Override
    protected String panicGuideUrl() { return "https://sites.google.com/a/ft.com/ft-technology-service-transition/home/run-book-library/<<!INSERT DOCUMENT WRITER RUNBOOK URL LOCATION HERE!>>"; }

}
