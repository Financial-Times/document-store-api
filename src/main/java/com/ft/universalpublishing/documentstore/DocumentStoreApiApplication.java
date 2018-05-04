package com.ft.universalpublishing.documentstore;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.platform.dropwizard.DefaultGoodToGoChecker;
import com.ft.platform.dropwizard.GoodToGoConfiguredBundle;
import com.ft.universalpublishing.documentstore.handler.ContentListValidationHandler;
import com.ft.universalpublishing.documentstore.handler.ExtractConceptHandler;
import com.ft.universalpublishing.documentstore.handler.ExtractUuidsHandler;
import com.ft.universalpublishing.documentstore.handler.Handler;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.handler.MultipleUuidValidationHandler;
import com.ft.universalpublishing.documentstore.handler.PreSaveFieldRemovalHandler;
import com.ft.universalpublishing.documentstore.handler.UuidValidationHandler;
import com.ft.universalpublishing.documentstore.health.DocumentStoreHealthCheck;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.resources.DocumentIDResource;
import com.ft.universalpublishing.documentstore.resources.DocumentQueryResource;
import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.service.filter.CacheControlFilter;
import com.ft.universalpublishing.documentstore.target.DeleteDocumentTarget;
import com.ft.universalpublishing.documentstore.target.FindListByConceptAndTypeTarget;
import com.ft.universalpublishing.documentstore.target.FindListByUuid;
import com.ft.universalpublishing.documentstore.target.FindMultipleResourcesByUuidsTarget;
import com.ft.universalpublishing.documentstore.target.FindResourceByUuidTarget;
import com.ft.universalpublishing.documentstore.target.Target;
import com.ft.universalpublishing.documentstore.target.WriteDocumentTarget;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocumentStoreApiApplication extends Application<DocumentStoreApiConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentStoreApiApplication.class);

    public static void main(final String[] args) throws Exception {
        new DocumentStoreApiApplication().run(args);
    }

    @Override
    public void initialize(final Bootstrap<DocumentStoreApiConfiguration> bootstrap) {
        bootstrap.addBundle(new GoodToGoConfiguredBundle(new DefaultGoodToGoChecker()));
        bootstrap.addBundle(new AdvancedHealthCheckBundle());
    }

    @Override
    public void run(final DocumentStoreApiConfiguration configuration, final Environment environment) {
        List<String> transactionUrlPattern = new ArrayList<>(
                Arrays.asList("/lists/*", "/content-query", "/content/*", "/internalcomponents/*", "/complementarycontent/*"));
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true,
                        transactionUrlPattern.toArray(new String[0]));

        environment.servlets().addFilter("cache-filter", new CacheControlFilter("max-age=" + configuration.getCacheTtl()))
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/lists/*");

        environment.jersey().register(new BuildInfoResource());

        MongoDatabase database = null;
        MongoDocumentStoreService documentStoreService;

        try {
            final MongoClient mongoClient = getMongoClient(configuration.getMongo());
            database = mongoClient.getDatabase(configuration.getMongo().getDb());
            documentStoreService = new MongoDocumentStoreService(database);

            // this will fail and timeout if MongoClient fails to connect to any configured node initially
            documentStoreService.applyIndexes();
            registerResources(configuration, environment, documentStoreService);
        } catch (final MongoException e) {
            logger.error("Failed to connect to mongo database, check/fix the issues and restart the service.", e);
        } finally {
            // We are registering health checks regardless, so that we can have proper visibility
            registerHealthChecks(configuration, environment, database);
        }
    }


    private void registerResources(DocumentStoreApiConfiguration configuration, Environment environment, MongoDocumentStoreService documentStoreService) {
        final UuidValidator uuidValidator = new UuidValidator();
        final ContentListValidator contentListValidator = new ContentListValidator(uuidValidator);

        Handler uuidValidationHandler = new UuidValidationHandler(uuidValidator);
        Handler multipleUuidValidationHandler = new MultipleUuidValidationHandler(uuidValidator);
        Handler extractUuidsHandlers = new ExtractUuidsHandler();
        Handler extractConceptHandler = new ExtractConceptHandler();
        Handler contentListValidationHandler = new ContentListValidationHandler(contentListValidator);
        Handler preSaveFieldRemovalHandler = new PreSaveFieldRemovalHandler();
        Target findResourceByUuid = new FindResourceByUuidTarget(documentStoreService);
        Target findMultipleResourcesByUuidsTarget = new FindMultipleResourcesByUuidsTarget(documentStoreService);
        Target writeDocument = new WriteDocumentTarget(documentStoreService);
        Target deleteDocument = new DeleteDocumentTarget(documentStoreService);
        Target findListByUuid = new FindListByUuid(documentStoreService, configuration.getApiHost());
        Target findListByConceptAndType = new FindListByConceptAndTypeTarget(documentStoreService, configuration.getApiHost());

        final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();
        collections.put(new Pair<>("content", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("content", Operation.GET_MULTIPLE_FILTERED),
                new HandlerChain().addHandlers(multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("content", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
        collections.put(new Pair<>("content", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler, preSaveFieldRemovalHandler).setTarget(writeDocument));
        collections.put(new Pair<>("content", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        collections.put(new Pair<>("complementarycontent", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("complementarycontent", Operation.GET_MULTIPLE_FILTERED),
                new HandlerChain().addHandlers(multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("complementarycontent", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
        collections.put(new Pair<>("complementarycontent", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
        collections.put(new Pair<>("complementarycontent", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        collections.put(new Pair<>("internalcomponents", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("internalcomponents", Operation.GET_MULTIPLE_FILTERED),
                new HandlerChain().addHandlers(multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("internalcomponents", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
        collections.put(new Pair<>("internalcomponents", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
        collections.put(new Pair<>("internalcomponents", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        collections.put(new Pair<>("lists", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractConceptHandler).setTarget(findListByConceptAndType));
        collections.put(new Pair<>("lists", Operation.GET_MULTIPLE_FILTERED),
                new HandlerChain().addHandlers(multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("lists", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findListByUuid));
        collections.put(new Pair<>("lists", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler, contentListValidationHandler).setTarget(writeDocument));
        collections.put(new Pair<>("lists", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        environment.jersey().register(new DocumentResource(collections));
        environment.jersey().register(new DocumentQueryResource(documentStoreService, configuration.getApiHost()));
        environment.jersey().register(new DocumentIDResource(documentStoreService));

    }

    void registerHealthChecks(DocumentStoreApiConfiguration configuration, Environment environment, MongoDatabase database) {
        environment.healthChecks().register(configuration.getHealthcheckParameters().getName(),
                new DocumentStoreHealthCheck(database, configuration.getHealthcheckParameters()));
    }

    private MongoClient getMongoClient(MongoConfig config) {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        Duration idleTimeoutDuration = Optional.ofNullable(config.getIdleTimeout()).orElse(Duration.minutes(10));
        int idleTimeout = (int) idleTimeoutDuration.toMilliseconds();
        builder.maxConnectionIdleTime(idleTimeout);

        Optional.ofNullable(config.getServerSelectorTimeout())
                .ifPresent(duration -> {
                    int serverSelectorTimeout = (int) duration.toMilliseconds();
                    builder.serverSelectionTimeout(serverSelectorTimeout);
                });

        List<ServerAddress> mongoServers = config.toServerAddresses();
        if (mongoServers.size() == 1) {
            // singleton configuration
            ServerAddress mongoServer = mongoServers.get(0);
            return new MongoClient(mongoServer, builder.build());
        } else {
            // cluster configuration
            return new MongoClient(mongoServers, builder.build());
        }
    }
}
