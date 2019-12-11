package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.IDStreamingException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.ACCEPT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.MESSAGE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.bson.Document.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class MongoDocumentStoreService {

    private static final String LISTS_COLLECTION = "lists";
    private static final String IDENT_AUTHORITY = "identifiers.authority";
    private static final String IDENT_VALUE = "identifiers.identifierValue";
    private static final String CONCEPT_UUID = "concept.uuid";
    private static final String LIST_TYPE = "listType";

    private final MongoDatabase db;
    private ExecutorService exec;
    private boolean indexed;
    private Runnable reindexer = () -> applyIndexes();
    private FluentLoggingWrapper logger;

    public MongoDocumentStoreService(final MongoDatabase db, ExecutorService exec) {
        this.db = db;
        this.exec = exec;
        exec.submit(reindexer);
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    public boolean isConnected() {
        boolean connected = false;

        logger.withMetodName("isConnected").withField(METHOD, GET).withField(ACCEPT, APPLICATION_JSON_TYPE);

        try {
            Document commandResult = db.runCommand(parse("{ serverStatus : 1 }"));
            connected = !commandResult.isEmpty();
        } catch (MongoException e) {
            logger.withException(e).withField(MESSAGE, "Cannot verify MongoDB connection").build().logWarn();
        }

        if (connected && !indexed) {
            // maybe we made a new connection, ensure indexes are created
            exec.submit(reindexer);
        } else if (!connected) {
            // we lost a connection, assume indexes are not up to date
            indexed = false;
        }

        return connected;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public Map<String, Object> findByUuid(String resourceType, UUID uuid, String method) {
        String errorMessage = null;
        
        logger.withMetodName("findByUuid").withField(METHOD, method).withField(ACCEPT, APPLICATION_JSON_TYPE);
        
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document foundDocument = dbCollection.find().filter(eq("uuid", uuid.toString())).first();
            if (foundDocument == null) {
                throw new DocumentNotFoundException(uuid);
            }

            foundDocument.remove("_id");
            return foundDocument;
        } catch (MongoSocketException | MongoTimeoutException e) {
            errorMessage = "MongoDB connection timed out or caused a socket exception during delete, "
                    + "please check MongoDB! Collection " + resourceType + ", uuids " + uuid;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            errorMessage = "Failed to find document in Mongo! Collection "
                    + resourceType + ", uuids " + uuid;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Set<Map<String, Object>> findByUuids(String resourceType, Set<UUID> uuids, String method) {
        String errorMessage = null;

        logger.withMetodName("findByUuids").withField(METHOD, method).withField(ACCEPT, APPLICATION_JSON_TYPE);

        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);

            Iterable<Document> results = dbCollection.find().filter(
                    in("uuid", uuids.stream().map(UUID::toString).collect(Collectors.toList())));

            Map<UUID, Document> mappedResults = new HashMap<>();
            results.forEach(doc -> mappedResults.put(fromString((String) doc.get("uuid")), doc));

            // preserve the order of the queried UUIDs in the found documents
            Set<Map<String, Object>> documents = new LinkedHashSet<>();
            uuids.forEach(uuid -> {
                Document doc = mappedResults.get(uuid);
                if (doc != null) {
                    doc.remove("_id");
                    documents.add(doc);
                }
            });

            return documents;
        } catch (MongoSocketException | MongoTimeoutException e) {
            errorMessage = "MongoDB connection timed out or caused a socket exception during delete,"
                    + " please check MongoDB! Collection " + resourceType + ", uuids " + uuids.toString();
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            errorMessage = "Failed to find document(s) in Mongo! Collection "
                    + resourceType + ", uuids " + uuids.toString();
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Map<String, Object> findByIdentifier(String resourceType, String authority, String identifierValue) {
        String errorMessage = null;

        logger.withMetodName("findByIdentifier").withField(METHOD, GET).withField(ACCEPT, APPLICATION_JSON_TYPE);
        
        Bson filter = and(
                eq("identifiers.authority", authority),
                eq("identifiers.identifierValue", identifierValue)
        );

        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document found = null;

            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                } else {
                    errorMessage = "found too many results for collection " + resourceType + " identifier " + authority
                            + ":" + identifierValue + ": at least " + found + " and " + doc;
                    logger.withField(MESSAGE, errorMessage).build().logError();
                    throw new QueryResultNotUniqueException();
                }
            }

            return found;
        } catch (MongoException e) {
            errorMessage = "Failed to find document in Mongo! Collection " + resourceType + ", authority " + authority
                    + ", identifierValue " + identifierValue;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Map<String, Object> findByConceptAndType(String resourceType, UUID conceptId,
            String listType, String method) {
        String errorMessage = null;

        logger.withMetodName("findByConceptAndType").withField(METHOD, method).withField(ACCEPT, APPLICATION_JSON_TYPE);

        Bson filter = and(
                eq("concept.uuid", conceptId.toString()),
                eq("listType", listType)
        );

        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document found = null;

            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                } else {
                    errorMessage = "found too many results for collection " + resourceType + " identifier " + conceptId
                            + ":" + listType + ": at least " + found + " and " + doc;
                    logger.withField(MESSAGE, errorMessage).build().logError();
                    return found; // just return the first one we found (graceful degradation) and log the error
                }
            }

            return found;
        } catch (MongoException e) {
            errorMessage = "Failed to find document in Mongo! Collection " + resourceType + ", uuid " + conceptId
                    + ", listType " + listType;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public void delete(String resourceType, UUID uuid, String method) {
        String errorMessage = null;

        logger.withMetodName("delete").withField(METHOD, method).withField(ACCEPT, APPLICATION_JSON_TYPE);

        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            DeleteResult deleteResult = dbCollection.deleteOne(eq("uuid", uuid.toString()));

            if (deleteResult.getDeletedCount() == 0) {
                throw new DocumentNotFoundException(uuid);
            }

        } catch (MongoSocketException | MongoTimeoutException e) {
            errorMessage = "MongoDB connection timed out or caused a socket exception during delete, "
                    + "please check MongoDB! Collection " + resourceType + ", uuid " + uuid;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            errorMessage = "Failed to delete document to Mongo! Collection "
                    + resourceType + ", uuid " + uuid;
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public DocumentWritten write(String resourceType, Map<String, Object> content, String method) {
        String errorMessage = null;
        
        logger.withMetodName("write").withField(METHOD, method).withField(ACCEPT, APPLICATION_JSON_TYPE);

        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            final String uuid = (String) content.get("uuid");
            Document document = new Document(content);
            UpdateResult updateResult = dbCollection.replaceOne(eq("uuid", uuid), document, new UpdateOptions().upsert(true));
            if (updateResult.getUpsertedId() == null) {
                return DocumentWritten.updated(document);
            }
            return DocumentWritten.created(document);
        } catch (MongoSocketException | MongoTimeoutException e) {
            errorMessage = "MongoDB connection timed out or caused a socket exception during write, "
                    + "please check MongoDB! Collection "
                    + resourceType + ", uuid " + content.get("uuid");
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            errorMessage = "Failed to write document to Mongo! Collection "
                    + resourceType + ", uuid " + content.get("uuid");
            logger.withException(e).withField(MESSAGE, errorMessage).build().logError();
            
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public void applyIndexes() {
        applyIndexForCollection("content");
        applyIndexForCollection("internalcomponents");
        applyIndexForListCollection();
        indexed = true;
    }

    @SuppressWarnings("rawtypes")
    private void applyIndexForListCollection() {
        MongoCollection lists = db.getCollection(LISTS_COLLECTION);
        createUuidIndex(lists);
        logger.withMetodName("applyIndexForListCollection")
                .withField(MESSAGE, "Created UUID index on collection " + LISTS_COLLECTION).build().logInfo();
        createConceptAndListTypeIndex(lists);
    }

    @SuppressWarnings("rawtypes")
    private void applyIndexForCollection(String collection) {
        MongoCollection mongoCollection = db.getCollection(collection);
        createUuidIndex(mongoCollection);
        logger.withMetodName("applyIndexForCollection")
                .withField(MESSAGE, "Created UUID index on collection " + collection).build().logInfo();
        createIdentifierIndex(mongoCollection);
    }

    private void createUuidIndex(MongoCollection<?> collection) {
        collection.createIndex(new Document("uuid", 1), new IndexOptions().background(true).unique(true));
    }

    private void createIdentifierIndex(MongoCollection<?> collection) {
        Document queryByIdentifierIndex = new Document();
        queryByIdentifierIndex.put(IDENT_AUTHORITY, 1);
        queryByIdentifierIndex.put(IDENT_VALUE, 1);
        collection.createIndex(queryByIdentifierIndex);
    }

    private void createConceptAndListTypeIndex(MongoCollection<?> collection) {
        Document queryByIdentifierIndex = new Document();
        queryByIdentifierIndex.put(CONCEPT_UUID, 1);
        queryByIdentifierIndex.put(LIST_TYPE, 1);
        collection.createIndex(queryByIdentifierIndex, new IndexOptions().background(true));
    }

    public void findUUIDs(String resourceType, boolean includeSource, OutputStream outputStream) {
        MongoCollection<Document> collection = db.getCollection(resourceType);
        MongoCursor<Document> cursor = getFindUUIDsQuery(collection, includeSource).iterator();

        logger.withMetodName("findUUIDs").withField(METHOD, GET).withField(ACCEPT, APPLICATION_JSON_TYPE);
        
        try {
            while (cursor.hasNext()){
                Document document = cursor.next();
                outputStream.write((document.toJson() + "\n").getBytes());
            }
            outputStream.flush();
        }catch (IOException e) {
            logger.withException(e).withField(MESSAGE, "Error occurred while trying to return ids").build().logError();
            
            throw new IDStreamingException(resourceType);
        }
    }

    private FindIterable<Document> getFindUUIDsQuery(MongoCollection<Document> collection, boolean includeSource) {
        List<Bson> projections = new ArrayList<>(asList(include("uuid"), excludeId()));
        if (includeSource) {
            projections.add(include(IDENT_AUTHORITY));
        }
        return collection.find().projection(fields(projections));
    }
}
