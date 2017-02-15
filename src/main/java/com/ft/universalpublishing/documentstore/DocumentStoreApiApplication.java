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
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.DispatcherType;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;



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
        List<String> transactionUrlPattern = new ArrayList<>(
                Arrays.asList("/lists/*", "/content-query", "/content/*", "/internalcomponents/*"));
        environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true,
                        transactionUrlPattern.toArray(new String[0]));

        environment.servlets().addFilter("cache-filter", new CacheControlFilter("max-age=" + configuration.getCacheTtl()))
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/lists/*");

        environment.jersey().register(new BuildInfoResource());

        final MongoClient mongoClient = getMongoClient(configuration.getMongo());
        MongoDatabase database = mongoClient.getDatabase(configuration.getMongo().getDb());

        final MongoDocumentStoreService documentStoreService = new MongoDocumentStoreService(database);
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
        collections.put(new Pair<>("content", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
        collections.put(new Pair<>("content", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler, preSaveFieldRemovalHandler).setTarget(writeDocument));
        collections.put(new Pair<>("content", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        collections.put(new Pair<>("internalcomponents", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
        collections.put(new Pair<>("internalcomponents", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
        collections.put(new Pair<>("internalcomponents", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
        collections.put(new Pair<>("internalcomponents", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        collections.put(new Pair<>("lists", Operation.GET_FILTERED),
                new HandlerChain().addHandlers(extractConceptHandler).setTarget(findListByConceptAndType));
        collections.put(new Pair<>("lists", Operation.GET_BY_ID),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findListByUuid));
        collections.put(new Pair<>("lists", Operation.ADD),
                new HandlerChain().addHandlers(uuidValidationHandler, contentListValidationHandler).setTarget(writeDocument));
        collections.put(new Pair<>("lists", Operation.REMOVE),
                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

        environment.jersey().register(new DocumentResource(collections));
        environment.jersey().register(new DocumentQueryResource(documentStoreService, configuration.getApiHost()));

        environment.healthChecks().register(configuration.getHealthcheckParameters().getName(), new DocumentStoreHealthCheck(database, configuration.getHealthcheckParameters()));

        documentStoreService.applyIndexes();
    }

    private MongoClient getMongoClient(MongoConfig config) {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        Duration idleTimeoutDuration = Optional.ofNullable(config.getIdleTimeout()).orElse(Duration.minutes(10));
        int idleTimeout = (int) idleTimeoutDuration.toMilliseconds();
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
