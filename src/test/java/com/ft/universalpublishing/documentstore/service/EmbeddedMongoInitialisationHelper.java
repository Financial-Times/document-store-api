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

/**
 * Created by julia.fernee on 03/11/2015.
 */
public class EmbeddedMongoInitialisationHelper {
    private static final String EMBEDDED_MONGODB_HOST = "localhost";
    private static final int EMBEDDED_MONGODB_SERVER_PORT = 12032;

    private MongodStarter starter = MongodStarter.getDefaultInstance();
    private MongodExecutable mongodExecutable = null;

    public EmbeddedMongoInitialisationHelper() {
        createEmbededMongoInstance();
    }

    private void createEmbededMongoInstance() {
        IMongodConfig mongodConfig = null;
        try {
            mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.V3_0)
                    .net(new Net(EMBEDDED_MONGODB_SERVER_PORT, Network.localhostIsIPv6()))
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

        List<ServerAddress> mongoServers = Collections.singletonList(new ServerAddress(EMBEDDED_MONGODB_HOST, EMBEDDED_MONGODB_SERVER_PORT));
        // cluster configuration
        MongoClient mongoClient = new MongoClient(mongoServers, options);
        
        return mongoClient;
    }

    public void shutdownGracefully() {
        if (this.mongodExecutable != null) {
            this.mongodExecutable.stop();
        }
    }
}
