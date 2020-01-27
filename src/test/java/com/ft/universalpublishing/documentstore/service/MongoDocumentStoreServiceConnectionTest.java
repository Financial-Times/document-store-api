package com.ft.universalpublishing.documentstore.service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MongoDocumentStoreServiceConnectionTest {

    private static final Document CONNECTED = new Document("ok", "true");

    private MongoDocumentStoreService service;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Mock
    private MongoDatabase db;

    @Mock
    private MongoCollection<Document> collection;

    @Test
    public void thatIndexesAreAppliedOnStartup() {
        when(db.getCollection(anyString())).thenReturn(collection);

        service = new MongoDocumentStoreService(db, executor);

        assertIsIndexed();
    }

    private void assertIsIndexed() {
        // a short wait for the thread to run
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        assertTrue(service.isIndexed());
    }

    @Test
    public void thatIsConnectedReturnsTrueWhenConnected() {
        when(db.runCommand(any(Bson.class))).thenReturn(CONNECTED);

        service = new MongoDocumentStoreService(db, executor);
        assertTrue(service.isConnected());
    }

    @Test
    public void thatIsConnectedReturnsFalseWhenNotConnected() {
        when(db.runCommand(any(Bson.class))).thenReturn(new Document());

        service = new MongoDocumentStoreService(db, executor);
        assertFalse(service.isConnected());
        assertFalse(service.isIndexed());
    }

    @Test
    public void thatIsConnectedReturnsFalseWhenConnectionError() {
        when(db.runCommand(any(Bson.class))).thenThrow(new MongoException("test exception"));

        service = new MongoDocumentStoreService(db, executor);
        assertFalse(service.isConnected());
        assertFalse(service.isIndexed());
    }

    @Test
    public void thatIndexIsReappliedAfterReconnection() {
        when(db.runCommand(any(Bson.class))).thenThrow(new MongoException("test exception"));
        when(db.getCollection(anyString())).thenThrow(new MongoException("test exception"));

        service = new MongoDocumentStoreService(db, executor);
        assertFalse(service.isConnected());
        assertFalse(service.isIndexed());

        // but on the next attempt
        reset(db);
        when(db.runCommand(any(Bson.class))).thenReturn(CONNECTED);
        when(db.getCollection(anyString())).thenReturn(collection);

        assertTrue(service.isConnected());
        assertIsIndexed();
    }

    @Test
    public void thatIndexStateIsDiscardedAfterConnectionDropped() {
        when(db.runCommand(any(Bson.class))).thenReturn(CONNECTED);
        when(db.getCollection(anyString())).thenReturn(collection);

        service = new MongoDocumentStoreService(db, executor);
        assertTrue(service.isConnected());
        assertIsIndexed();

        // but on the next attempt
        reset(db);
        when(db.runCommand(any(Bson.class))).thenThrow(new MongoException("test exception"));

        assertFalse(service.isConnected());
        assertFalse(service.isIndexed());
    }
}
