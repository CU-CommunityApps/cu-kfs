package edu.cornell.kfs.sys.service;

import java.text.ParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface AwsSecretService {
    
    String getSingleStringValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue);
    
    Date getSingleDateValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException;
    
    void updateSecretDate(String awsKeyName, boolean useKfsInstanceNamespace, Date date);
    
    boolean getSingleBooleanFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretBoolean(String awsKeyName, boolean useKfsInstanceNamespace,  boolean booleanValue);
    
    float getSingleNumberValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretNumber(String awsKeyName, boolean useKfsInstanceNamespace,  float numericValue);

    <T> T getPojoFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) throws JsonMappingException, JsonProcessingException;
    
    void updatePojo(String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) throws JsonProcessingException;

}
