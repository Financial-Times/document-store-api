package com.ft.universalpublishing.documentstore.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Builder;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Collections;

@Builder
public class EmbeddedMongoExtension implements BeforeEachCallback {

    private String dbName;

    private String dbCollection;

    @Getter
    private MongoDatabase db;

    public MongoClient client() {
        ServerAddress serverAddress;
        String shortTest = System.getProperty("short");
        //triggers a test skip if assumption is not met
        Assume.assumeTrue(shortTest == null);

        String mongoTestUrl = System.getenv("MONGO_TEST_URL");
        if (mongoTestUrl == null) {
            Assert.fail("System property MONGO_TEST_URL should be set to a valid mongo server instance, e.g. MONGO_TEST_URL=localhost:27017. Alternatively you could skip these tests by providig the -Dshort java program flag");
        }

        String[] urlAddress = mongoTestUrl.replaceAll("https?://", "").split(":");
        if (urlAddress.length != 2) {
            Assert.fail("System property MONGO_TEST_URL should be set to a valid mongo server instance, e.g. MONGO_TEST_URL=localhost:27017. Alternatively you could skip these tests by providig the -Dshort java program flag");
        }

        serverAddress = new ServerAddress(urlAddress[0], Integer.parseInt(urlAddress[1]));

        MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
                .serverSelectionTimeout(1000)
                .connectTimeout(1000)
                .socketTimeout(1000)
                .build();

        return new MongoClient(Collections.singletonList(serverAddress), mongoClientOptions);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        this.db = client().getDatabase(dbName);
        this.db.getCollection(dbCollection).drop();
    }
}
