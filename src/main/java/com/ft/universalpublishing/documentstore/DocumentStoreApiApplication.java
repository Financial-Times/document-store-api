package com.ft.universalpublishing.documentstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;

import com.ft.api.util.buildinfo.BuildInfoResource;
import com.ft.api.util.transactionid.TransactionIdFilter;
import com.ft.platform.dropwizard.AdvancedHealthCheckBundle;
import com.ft.platform.dropwizard.GoodToGoConfiguredBundle;
import com.ft.universalpublishing.documentstore.clients.PublicConceptsApiClient;
import com.ft.universalpublishing.documentstore.clients.PublicConcordancesApiClient;
import com.ft.universalpublishing.documentstore.handler.ContentListValidationHandler;
import com.ft.universalpublishing.documentstore.handler.ExtractConceptHandler;
import com.ft.universalpublishing.documentstore.handler.ExtractUuidsHandler;
import com.ft.universalpublishing.documentstore.handler.FilterListsHandler;
import com.ft.universalpublishing.documentstore.handler.FindListByConceptAndTypeHandler;
import com.ft.universalpublishing.documentstore.handler.FindListByUuidHandler;
import com.ft.universalpublishing.documentstore.handler.GetConcordedConceptsHandler;
import com.ft.universalpublishing.documentstore.handler.Handler;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.handler.MultipleUuidValidationHandler;
import com.ft.universalpublishing.documentstore.handler.PreSaveFieldRemovalHandler;
import com.ft.universalpublishing.documentstore.handler.UuidValidationHandler;
import com.ft.universalpublishing.documentstore.health.DocumentStoreConnectionGoodToGoChecker;
import com.ft.universalpublishing.documentstore.health.DocumentStoreConnectionHealthCheck;
import com.ft.universalpublishing.documentstore.health.DocumentStoreIndexHealthCheck;
import com.ft.universalpublishing.documentstore.health.GenericDocumentStoreHealthCheck;
import com.ft.universalpublishing.documentstore.health.HealthcheckParameters;
import com.ft.universalpublishing.documentstore.health.HealthcheckService;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.resources.DocumentIDResource;
import com.ft.universalpublishing.documentstore.resources.DocumentQueryResource;
import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiServiceImpl;
import com.ft.universalpublishing.documentstore.service.PublicConcordancesApiService;
import com.ft.universalpublishing.documentstore.service.PublicConcordancesApiServiceImpl;
import com.ft.universalpublishing.documentstore.service.filter.CacheControlFilter;
import com.ft.universalpublishing.documentstore.target.ApplyConcordedConceptToList;
import com.ft.universalpublishing.documentstore.target.ApplyConcordedConceptsToLists;
import com.ft.universalpublishing.documentstore.target.DeleteDocumentTarget;
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

import org.glassfish.jersey.client.JerseyClientBuilder;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

@SwaggerDefinition(tags = {
                @Tag(name = "collections", description = "Operations on document-store MongoDB collections") })
public class DocumentStoreApiApplication extends Application<DocumentStoreApiConfiguration> {

        public static void main(final String[] args) throws Exception {
                new DocumentStoreApiApplication().run(args);
        }

        @Override
        public void initialize(final Bootstrap<DocumentStoreApiConfiguration> bootstrap) {
                bootstrap.addBundle(new GoodToGoConfiguredBundle(new DocumentStoreConnectionGoodToGoChecker()));
                bootstrap.addBundle(new AdvancedHealthCheckBundle());

                bootstrap.addBundle(new SwaggerBundle<DocumentStoreApiConfiguration>() {
                        @Override
                        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                                        DocumentStoreApiConfiguration config) {
                                return config.swaggerBundleConfiguration;
                        }
                });
        }

        @Override
        public void run(final DocumentStoreApiConfiguration configuration, final Environment environment) {
                List<String> transactionUrlPattern = new ArrayList<>(Arrays.asList("/generic-lists/*", "/lists/*",
                                "/content-query", "/content/*", "/internalcomponents/*", "/complementarycontent/*"));
                environment.servlets().addFilter("transactionIdFilter", new TransactionIdFilter())
                                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true,
                                                transactionUrlPattern.toArray(new String[0]));

                environment.servlets()
                                .addFilter("cache-filter",
                                                new CacheControlFilter("max-age=" + configuration.getCacheTtl()))
                                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/lists/*");

                environment.jersey().register(new BuildInfoResource());

                final MongoClient mongoClient = getMongoClient(configuration.getMongo());
                final MongoDatabase database = mongoClient.getDatabase(configuration.getMongo().getDb());
                final MongoDocumentStoreService documentStoreService = new MongoDocumentStoreService(database,
                                environment.lifecycle().executorService("reindexer").build());

                Client client = new JerseyClientBuilder().build();
                PublicConceptsApiClient publicConceptsApiClient = new PublicConceptsApiClient(
                                configuration.getPublicConceptsApiConfig().getHost(), client);

                PublicConcordancesApiClient publicConcordancesApiClient = new PublicConcordancesApiClient(
                                configuration.getPublicConcordancesApiConfig().getHost(), client);

                final PublicConceptsApiServiceImpl publicConceptsApiService = new PublicConceptsApiServiceImpl(
                                publicConceptsApiClient);
                final PublicConcordancesApiServiceImpl publicConcordancesApiService = new PublicConcordancesApiServiceImpl(
                                publicConcordancesApiClient);

                registerHealthChecks(configuration, environment, documentStoreService, publicConceptsApiService,
                                publicConcordancesApiService);
                registerResources(configuration, environment, documentStoreService, publicConceptsApiService,
                                publicConcordancesApiService);
        }

        private void registerResources(DocumentStoreApiConfiguration configuration, Environment environment,
                        MongoDocumentStoreService documentStoreService,
                        PublicConceptsApiService publicConceptsApiService,
                        PublicConcordancesApiService publicConcordancesApiService) {
                final UuidValidator uuidValidator = new UuidValidator();
                final ContentListValidator contentListValidator = new ContentListValidator(uuidValidator);

                Handler uuidValidationHandler = new UuidValidationHandler(uuidValidator);
                Handler multipleUuidValidationHandler = new MultipleUuidValidationHandler(uuidValidator);
                Handler extractUuidsHandlers = new ExtractUuidsHandler();
                Handler extractConceptHandler = new ExtractConceptHandler();
                Handler contentListValidationHandler = new ContentListValidationHandler(contentListValidator);
                Handler preSaveFieldRemovalHandler = new PreSaveFieldRemovalHandler();
                Handler findListByUuidHandler = new FindListByUuidHandler(documentStoreService,
                                configuration.getApiHost());
                Handler findListByConceptAndTypeHandler = new FindListByConceptAndTypeHandler(documentStoreService);
                Handler getConcordedConceptsHandler = new GetConcordedConceptsHandler(publicConcordancesApiService);
                Handler getSearchResultsHandler = new FilterListsHandler(documentStoreService);
                Target findResourceByUuid = new FindResourceByUuidTarget(documentStoreService);
                Target findMultipleResourcesByUuidsTarget = new FindMultipleResourcesByUuidsTarget(
                                documentStoreService);
                Target writeDocument = new WriteDocumentTarget(documentStoreService);
                Target deleteDocument = new DeleteDocumentTarget(documentStoreService);

                // TODO: remove class
                // Target findListByUuid = new FindListByUuid(documentStoreService,
                // configuration.getApiHost());
                // Target findListByConceptAndType = new
                // FindListByConceptAndTypeTarget(documentStoreService,
                // configuration.getApiHost());
                // Target getSearchResults = new FilterListsHandler(documentStoreService);
                Target applyConcordedConceptToList = new ApplyConcordedConceptToList(publicConceptsApiService,
                                configuration.getApiHost());
                Target applyConcordedConceptsToLists = new ApplyConcordedConceptsToLists(publicConceptsApiService,
                                configuration.getApiHost());

                final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();
                collections.put(new Pair<>("content", Operation.GET_FILTERED),
                                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("content", Operation.GET_MULTIPLE_FILTERED),
                                new HandlerChain().addHandlers(multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("content", Operation.GET_BY_ID),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
                collections.put(new Pair<>("content", Operation.ADD),
                                new HandlerChain().addHandlers(uuidValidationHandler, preSaveFieldRemovalHandler)
                                                .setTarget(writeDocument));
                collections.put(new Pair<>("content", Operation.REMOVE),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

                collections.put(new Pair<>("complementarycontent", Operation.GET_FILTERED),
                                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("complementarycontent", Operation.GET_MULTIPLE_FILTERED),
                                new HandlerChain().addHandlers(multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("complementarycontent", Operation.GET_BY_ID),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
                collections.put(new Pair<>("complementarycontent", Operation.ADD),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
                collections.put(new Pair<>("complementarycontent", Operation.REMOVE),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

                collections.put(new Pair<>("internalcomponents", Operation.GET_FILTERED),
                                new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("internalcomponents", Operation.GET_MULTIPLE_FILTERED),
                                new HandlerChain().addHandlers(multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("internalcomponents", Operation.GET_BY_ID),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
                collections.put(new Pair<>("internalcomponents", Operation.ADD),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
                collections.put(new Pair<>("internalcomponents", Operation.REMOVE),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

                // TODO: apply for both "lists" and "generic-lists"
                // pseudo
                // get by id
                collections.put(new Pair<>("lists", Operation.GET_BY_ID),
                                new HandlerChain().addHandlers(uuidValidationHandler, findListByUuidHandler)
                                                .setTarget(applyConcordedConceptToList));

                // // get by listTypeAndCuratedFor
                collections.put(new Pair<>("lists", Operation.GET_FILTERED),
                                new HandlerChain()
                                                .addHandlers(extractConceptHandler, getConcordedConceptsHandler,
                                                                findListByConceptAndTypeHandler)
                                                .setTarget(applyConcordedConceptToList));

                // // search GET lists?conceptUUID&listType&searchTerm - all params are optional
                // // todo: create new handler if there is a conceptUUID parameter -> search in
                // // public_concordance_api and save to
                // // todo: match concorded concepts that are returned from public-concepts-api
                // to
                // // the lists from mongo
                // collections.put(new Pair<>("lists", Operation.SEARCH),
                // new HandlerChain().addHandlers(getConcordedConceptsHandler,
                // getSearchResultsHandler)
                // .setTarget(applyConcordedConceptsToLists));

                // // search POST lists
                // // create a new handler that corresponds to
                // findMultipleResourcesByUuidsTarget
                // collections.put(new Pair<>("lists", Operation.GET_MULTIPLE_FILTERED),
                // new HandlerChain()
                // .addHandlers(multipleUuidValidationHandler,
                // findMultipleResourcesByUuidsTarget)
                // .setTarget(applyConcordedConceptsToLists));

                // collections.put(new Pair<>("lists", Operation.GET_FILTERED), new
                // HandlerChain()
                // .addHandlers(extractConceptHandler).setTarget(findListByConceptAndType));
                collections.put(new Pair<>("lists", Operation.GET_MULTIPLE_FILTERED),
                                new HandlerChain().addHandlers(multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                // collections.put(new Pair<>("lists", Operation.GET_BY_ID),
                // new
                // HandlerChain().addHandlers(uuidValidationHandler).setTarget(findListByUuid));
                collections.put(new Pair<>("lists", Operation.ADD),
                                new HandlerChain().addHandlers(uuidValidationHandler, contentListValidationHandler)
                                                .setTarget(writeDocument));
                collections.put(new Pair<>("lists", Operation.REMOVE),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));
                collections.put(new Pair<>("lists", Operation.SEARCH),
                                new HandlerChain().addHandlers(getConcordedConceptsHandler, getSearchResultsHandler)
                                                .setTarget(applyConcordedConceptsToLists));

                collections.put(new Pair<>("generic-lists", Operation.GET_FILTERED),
                                new HandlerChain()
                                                .addHandlers(extractConceptHandler, getConcordedConceptsHandler,
                                                                findListByConceptAndTypeHandler)
                                                .setTarget(applyConcordedConceptToList));
                collections.put(new Pair<>("generic-lists", Operation.GET_MULTIPLE_FILTERED),
                                new HandlerChain().addHandlers(multipleUuidValidationHandler)
                                                .setTarget(findMultipleResourcesByUuidsTarget));
                collections.put(new Pair<>("generic-lists", Operation.GET_BY_ID),
                                new HandlerChain().addHandlers(uuidValidationHandler, findListByUuidHandler)
                                                .setTarget(applyConcordedConceptToList));
                // collections.put(new Pair<>("generic-lists", Operation.GET_BY_ID),
                // new
                // HandlerChain().addHandlers(uuidValidationHandler).setTarget(findListByUuid));
                collections.put(new Pair<>("generic-lists", Operation.ADD),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(writeDocument));
                collections.put(new Pair<>("generic-lists", Operation.REMOVE),
                                new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));
                // collections.put(new Pair<>("generic-lists", Operation.SEARCH),
                // new HandlerChain().addHandlers(getConcordedConceptsHandler,
                // getSearchResultsHandler)
                // .setTarget(getSearchResults));

                environment.jersey().register(new DocumentResource(collections));
                environment.jersey()
                                .register(new DocumentQueryResource(documentStoreService, configuration.getApiHost()));
                environment.jersey().register(new DocumentIDResource(documentStoreService));

        }

        private void registerHealthChecks(DocumentStoreApiConfiguration configuration, Environment environment,
                        MongoDocumentStoreService service, HealthcheckService publicConceptsApiService,
                        HealthcheckService publicConcordancesApiService) {

                // TODO: REMOVE
                System.out.println(String.format("url: %s", configuration.getPublicConceptsApiConfig().getHost()));
                System.out.println(String.format("url: %s", configuration.getPublicConcordancesApiConfig().getHost()));

                HealthcheckParameters healthcheckParameters = configuration.getConnectionHealthcheckParameters();
                environment.healthChecks().register(healthcheckParameters.getName(),
                                new DocumentStoreConnectionHealthCheck(service, healthcheckParameters));

                healthcheckParameters = configuration.getIndexHealthcheckParameters();
                environment.healthChecks().register(healthcheckParameters.getName(),
                                new DocumentStoreIndexHealthCheck(service, healthcheckParameters));

                HealthcheckParameters publicConceptsApiHealthcheckParameters = configuration
                                .getPublicConceptsApiConfig().getHealthcheckParameters();
                environment.healthChecks().register(publicConceptsApiHealthcheckParameters.getName(),
                                new GenericDocumentStoreHealthCheck(publicConceptsApiService,
                                                publicConceptsApiHealthcheckParameters));

                HealthcheckParameters publicConcordancesApiHealthcheckParameters = configuration
                                .getPublicConcordancesApiConfig().getHealthcheckParameters();
                environment.healthChecks().register(publicConcordancesApiHealthcheckParameters.getName(),
                                new GenericDocumentStoreHealthCheck(publicConcordancesApiService,
                                                publicConcordancesApiHealthcheckParameters));
        }

        private MongoClient getMongoClient(MongoConfig config) {
                MongoClientOptions.Builder builder = MongoClientOptions.builder();

                Duration idleTimeoutDuration = Optional.ofNullable(config.getIdleTimeout())
                                .orElse(Duration.minutes(10));
                int idleTimeout = (int) idleTimeoutDuration.toMilliseconds();
                builder.maxConnectionIdleTime(idleTimeout);

                Optional.ofNullable(config.getServerSelectorTimeout()).ifPresent(duration -> {
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
