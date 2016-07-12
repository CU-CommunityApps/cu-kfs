package edu.cornell.kfs.sys.dataaccess.impl;

import com.mongodb.Mongo;
import edu.cornell.kfs.sys.dataaccess.DocumentstoreClient;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.net.UnknownHostException;


public class MongoDBClient implements DocumentstoreClient {

    private MongoTemplate mongoTemplate;

    public MongoDBClient(String host, int port, String dbName, String username, String password) {
        Mongo mongo = null;
        try {
            mongo = new Mongo(host, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        UserCredentials userCredentials = new UserCredentials(username, password);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongo, dbName, userCredentials);
        mongoTemplate = new MongoTemplate(mongoDbFactory);
    }

    public MongoTemplate getClient() {
        return mongoTemplate;
    }
}
