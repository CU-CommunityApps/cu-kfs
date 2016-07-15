package edu.cornell.kfs.sys.dataaccess.impl;

import com.mongodb.Mongo;
import edu.cornell.kfs.sys.dataaccess.DocumentStoreClient;
import org.apache.log4j.Logger;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.net.UnknownHostException;


public class MongoDBClient implements DocumentStoreClient {

    private static final Logger LOG = Logger.getLogger(MongoDBClient.class);

    private MongoTemplate mongoTemplate;

    public MongoDBClient(String protocol, String host, int port, String dbName, String username, String password) {
        Mongo mongo = null;
        try {
            mongo = new Mongo(host, port);
        } catch (UnknownHostException e) {
            LOG.error("MongoDBClient() - error creating new MongoDB Client", e);
            throw new RuntimeException("MongoDBClient() - error creating new MongoDB Client", e);
        }
        UserCredentials userCredentials = new UserCredentials(username, password);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongo, dbName, userCredentials);
        mongoTemplate = new MongoTemplate(mongoDbFactory);
    }

    public MongoTemplate getClient() {
        return mongoTemplate;
    }
}
