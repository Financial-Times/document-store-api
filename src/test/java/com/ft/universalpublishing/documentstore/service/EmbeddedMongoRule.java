package com.ft.universalpublishing.documentstore.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by julia.fernee on 03/11/2015.
 */
public class EmbeddedMongoRule
        implements TestRule {
    
    private int port;
    private MongodStarter starter = MongodStarter.getDefaultInstance();
    private MongodExecutable mongodExecutable = null;

    public EmbeddedMongoRule(int port) {
        this.port = port;
    }

    private void createEmbeddedMongoInstance(int port) {
        IMongodConfig mongodConfig = null;
        try {
            mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.V3_0)
                    .net(new Net(port, Network.localhostIsIPv6()))
                    .build();
            mongodExecutable = starter.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
            
        } catch (IOException e) {
            if (this.mongodExecutable != null) {
                this.mongodExecutable.stop();
            }
        }
    }
    
    public MongoClient client() {
        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
        MongoClientOptions options = optionsBuilder.build();

        List<ServerAddress> mongoServers = Collections.singletonList(new ServerAddress("localhost", port));
        // cluster configuration
        MongoClient mongoClient = new MongoClient(mongoServers, options);
        
        return mongoClient;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    createEmbeddedMongoInstance(port);
                    base.evaluate();
                }
                finally {
                    if (mongodExecutable != null) {
                        mongodExecutable.stop();
                    }
                }
            }
        };
    }
}
