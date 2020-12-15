package edu.cornell.kfs.sys.service.impl;

import java.util.Date;
import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.google.gson.Gson;

import edu.cornell.kfs.sys.jsonadapters.JsonDateSerializer;
import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretServiceImpl  implements AwsSecretService{
    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImpl.class);
    
    protected String awsRegion;
    protected String kfsInstanceNameSpace;
    protected String kfsSharedNamespace;
    
    @Override
    public String getSingleStringValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(fullAwsKey);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = buildAWSSecretsManager().getSecretValue(getSecretValueRequest);
        } catch (SdkClientException e) {
            LOG.error("getSingleStringValueFromAwsSecret, had an error getting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        }

        return getSecretValueResult.getSecretString();
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest ().withSecretId(fullAwsKey);
        updateSecretRequest.setSecretString(keyValue);
        buildAWSSecretsManager().updateSecret(updateSecretRequest);
    }
    
    protected AWSSecretsManager buildAWSSecretsManager() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion).build();
        return client;
    }
    
    protected String buildFullAwsKeyName(String awsKeyName, boolean useKfsInstanceNamespace) {
        String fullKeyName;
        if (useKfsInstanceNamespace) {
            fullKeyName = kfsInstanceNameSpace + awsKeyName;
        } else {
            fullKeyName = kfsSharedNamespace + awsKeyName; 
        }
        return fullKeyName;
    }
    
    @Override
    public Date getSingleDateValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException {
        String dateString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        return JsonDateSerializer.convertStringToDate(dateString);
    }
    
    @Override
    public void updateSecretDate(String awsKeyName, boolean useKfsInstanceNamespace, Date date) {
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, JsonDateSerializer.convertDateToString(date));
    }

    @Override
    public <T> T getPojoFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) {
        String pojoJsonString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        Gson gson = new Gson();
        T object = gson.fromJson(pojoJsonString, objectType);
        return object;
    }
    
    @Override
    public void updatePojo(String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pojo);
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, jsonString);
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public void setKfsInstanceNameSpace(String kfsInstanceNameSpace) {
        this.kfsInstanceNameSpace = kfsInstanceNameSpace;
    }

    public void setKfsSharedNamespace(String kfsSharedNamespace) {
        this.kfsSharedNamespace = kfsSharedNamespace;
    }

}
