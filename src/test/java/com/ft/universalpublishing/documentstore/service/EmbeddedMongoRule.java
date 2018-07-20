package com.ft.universalpublishing.documentstore.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collections;


public class EmbeddedMongoRule
        implements TestRule {

    public MongoClient client() {
        ServerAddress serverAddress;
        String[] urlAddress = null;
        String mongoTestUrl = System.getenv("MONGO_TEST_URL");
        if (mongoTestUrl != null) {
            mongoTestUrl = mongoTestUrl.replaceAll("https?://", "");
            urlAddress = mongoTestUrl.split(":");
        }

        if (urlAddress == null || urlAddress.length != 2) {
            serverAddress = new ServerAddress(ServerAddress.defaultHost(), ServerAddress.defaultPort());
        } else {
            serverAddress = new ServerAddress(urlAddress[0], Integer.parseInt(urlAddress[1]));
        }
        
        return new MongoClient(Collections.singletonList(serverAddress), MongoClientOptions.builder().build());
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
            }
        };
    }
}
