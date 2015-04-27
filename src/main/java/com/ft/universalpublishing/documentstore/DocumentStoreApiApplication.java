package com.ft.universalpublishing.documentstore;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.google.common.collect.Lists;
import com.mongodb.ServerAddress;
import com.ft.universalpublishing.documentstore.health.DocumentStoreHealthCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.ft.api.jaxrs.errors.RuntimeExceptionMapper;
import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.universalpublishing.documentstore.mongo.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.util.List;

public class DocumentStoreApiApplication extends Application<DocumentStoreApiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DocumentStoreApiApplication().run(args);
    }

    @Override
    public void initialize(final Bootstrap<DocumentStoreApiConfiguration> bootstrap) {
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final DocumentStoreApiConfiguration configuration, final Environment environment) throws Exception {
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/content/*", "/lists/*");
        
        environment.jersey().register(new BuildInfoResource());
        
        final MongoClient mongoClient = getMongoClient(configuration.getMongo());
        final DB db = mongoClient.getDB(configuration.getMongo().getDb());
        
        final DocumentStoreService documentStoreService = new MongoDocumentStoreService(db, configuration.getApiPath()); 
        final ContentDocumentValidator contentDocumentValidator = new ContentDocumentValidator();
        final UuidValidator uuidValidator = new UuidValidator();
        final ContentListDocumentValidator contentListDocumentValidator = new ContentListDocumentValidator(uuidValidator);

        environment.jersey().register(new DocumentResource(documentStoreService, contentDocumentValidator, contentListDocumentValidator, uuidValidator));
        environment.healthChecks().register(configuration.getHealthcheckParameters().getName(), new DocumentStoreHealthCheck(db, configuration.getHealthcheckParameters()));
        environment.jersey().register(new RuntimeExceptionMapper());

    }

    private MongoClient getMongoClient(MongoConfig config) {
        List<ServerAddress> mongoServers = config.toServerAddresses();
        if (mongoServers.size() == 1) {
            // singleton configuration
            ServerAddress mongoServer = mongoServers.get(0);
            return new MongoClient(mongoServer);
        } else {
            // cluster configuration
            return new MongoClient(mongoServers);
        }
    }

}
