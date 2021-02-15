package edu.cornell.kfs.sys.service;

import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface AwsSecretService {
    
    String getSingleStringValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretValue(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, String keyValue);
    
    Date getSingleDateValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException;
    
    void updateSecretDate(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Date date);
    
    boolean getSingleBooleanFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretBoolean(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace,  boolean booleanValue);
    
    float getSingleNumberValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretNumber(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace,  float numericValue);

    <T> T getPojoFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) throws JsonMappingException, JsonProcessingException;
    
    void updatePojo(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) throws JsonProcessingException;
    
    void initializeCache(Class cacheScope);
    
    void clearCache(Class cacheScope);

}
