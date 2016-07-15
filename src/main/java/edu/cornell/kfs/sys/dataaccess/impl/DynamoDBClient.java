package edu.cornell.kfs.sys.dataaccess.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import edu.cornell.kfs.sys.dataaccess.DocumentStoreClient;

public class DynamoDBClient implements DocumentStoreClient {

    private AmazonDynamoDBClient dynamoDBClient;

    public DynamoDBClient(String protocol, String host, int port, String dbName, String username, String password) {
        AWSCredentials credentials = new BasicAWSCredentials(username, password);
        dynamoDBClient = new AmazonDynamoDBClient(credentials);
        dynamoDBClient.setEndpoint(protocol + "://" + host + ":" + port);
    }

    public AmazonDynamoDBClient getClient() {
        return dynamoDBClient;
    }
}
