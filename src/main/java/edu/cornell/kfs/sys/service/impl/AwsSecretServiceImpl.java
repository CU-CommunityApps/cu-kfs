package edu.cornell.kfs.sys.service.impl;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.jsonadapters.JsonDateSerializer;
import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretServiceImpl  implements AwsSecretService{
    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImpl.class);
    
    protected String awsRegion;
    
    @Override
    public String getSingleStringValueFromAwsSecret(String awsKeyName) {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion).build();
        String secret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(awsKeyName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException | InternalServiceErrorException | InvalidParameterException
                | InvalidRequestException | ResourceNotFoundException e) {
            LOG.error("getSingleStringValueFromAwsSecret, had an error get value for secret " + awsKeyName, e);
            throw new RuntimeException(e);
        }

        secret = getSecretValueResult.getSecretString();
        return secret;
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, String keyValue) {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(awsRegion).build();
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest ().withSecretId (awsKeyName);
        updateSecretRequest.setSecretString(keyValue);
        client.updateSecret(updateSecretRequest);
    }
    
    @Override
    public Date getSingleDateValueFromAwsSecret(String awsKeyName) throws ParseException {
        String dateString = getSingleStringValueFromAwsSecret(awsKeyName);
        return JsonDateSerializer.convertStringToDate(dateString);
    }
    
    @Override
    public void updateSecretDate(String awsKeyName, Date date) {
        updateSecretValue(awsKeyName, JsonDateSerializer.convertDateToString(date));
    }

    @Override
    public <T> T getPojoFromAwsSecret(String awsKeyName, Class<T> objectType) {
        String pojoJsonString = getSingleStringValueFromAwsSecret(awsKeyName);
        Gson gson = new Gson();
        T object = gson.fromJson(pojoJsonString, objectType);
        return object;
    }
    
    @Override
    public void updatePojo(String awsKeyName, Object pojo) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(pojo);
        updateSecretValue(awsKeyName, jsonString);
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

}
