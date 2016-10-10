package com.ft.universalpublishing.documentstore;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.platform.dropwizard.DefaultGoodToGoChecker;
import com.ft.platform.dropwizard.GoodToGoConfiguredBundle;
import com.ft.universalpublishing.documentstore.health.DocumentStoreHealthCheck;
import com.ft.universalpublishing.documentstore.resources.DocumentQueryResource;
import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.ft.universalpublishing.documentstore.resources.DocumentStoreExceptionMapper;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.service.filter.CacheControlFilter;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;


public class DocumentStoreApiApplication extends Application<DocumentStoreApiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DocumentStoreApiApplication().run(args);
    }

    @Override
    public void initialize(final Bootstrap<DocumentStoreApiConfiguration> bootstrap) {
        bootstrap.addBundle(new GoodToGoConfiguredBundle(new DefaultGoodToGoChecker()));
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final DocumentStoreApiConfiguration configuration, final Environment environment) throws Exception {
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/content/*", "/lists/*", "/content-query");

        environment.servlets().addFilter("cache-filter", new CacheControlFilter("max-age=" + configuration.getCacheTtl()))
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/lists/*");

        environment.jersey().register(new BuildInfoResource());

        final MongoClient mongoClient = getMongoClient(configuration.getMongo());
        MongoDatabase database = mongoClient.getDatabase(configuration.getMongo().getDb());

        final MongoDocumentStoreService documentStoreService = new MongoDocumentStoreService(database);
        final UuidValidator uuidValidator = new UuidValidator();
        final ContentListValidator contentListValidator = new ContentListValidator(uuidValidator);

        environment.jersey().register(new DocumentResource(documentStoreService, contentListValidator, uuidValidator, configuration.getApiHost()));
        environment.jersey().register(new DocumentQueryResource(documentStoreService, configuration.getApiHost()));
        environment.jersey().register(DocumentStoreExceptionMapper.class);

        environment.healthChecks().register(configuration.getHealthcheckParameters().getName(), new DocumentStoreHealthCheck(database, configuration.getHealthcheckParameters()));

        documentStoreService.applyIndexes();
    }
    
    private MongoClient getMongoClient(MongoConfig config) {
      MongoClientOptions.Builder builder = MongoClientOptions.builder();
      
      Duration idleTimeoutDuration = Optional.ofNullable(config.getIdleTimeout()).orElse(Duration.minutes(10));
      int idleTimeout = (int)idleTimeoutDuration.toMilliseconds();
      MongoClientOptions options = builder.maxConnectionIdleTime(idleTimeout).build();
      
        List<ServerAddress> mongoServers = config.toServerAddresses();
        if (mongoServers.size() == 1) {
            // singleton configuration
            ServerAddress mongoServer = mongoServers.get(0);
            return new MongoClient(mongoServer, options);
        } else {
            // cluster configuration
            return new MongoClient(mongoServers, options);
        }
    }
}
