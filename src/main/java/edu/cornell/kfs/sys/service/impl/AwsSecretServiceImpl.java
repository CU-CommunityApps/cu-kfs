package edu.cornell.kfs.sys.service.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.EncryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.LimitExceededException;
import com.amazonaws.services.secretsmanager.model.MalformedPolicyDocumentException;
import com.amazonaws.services.secretsmanager.model.PreconditionNotMetException;
import com.amazonaws.services.secretsmanager.model.ResourceExistsException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;
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
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException | InternalServiceErrorException | InvalidParameterException | 
                InvalidRequestException | ResourceNotFoundException e) {
            LOG.error("getSingleStringValueFromAwsSecret, had an error getting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }

        return getSecretValueResult.getSecretString();
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest ().withSecretId(fullAwsKey);
        updateSecretRequest.setSecretString(keyValue);
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            perfornUpdate(updateSecretRequest, client);
        } catch (EncryptionFailureException | InternalServiceErrorException | InvalidParameterException | LimitExceededException |
                InvalidRequestException | ResourceNotFoundException | ResourceExistsException | MalformedPolicyDocumentException |
                PreconditionNotMetException e) {
            LOG.error("updateSecretValue, had an error setting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }
    }

    protected void perfornUpdate(UpdateSecretRequest updateSecretRequest, AWSSecretsManager client) {
        boolean processed = false;
        int tryCount = 0;
        while(!processed) {
            UpdateSecretResult result = client.updateSecret(updateSecretRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
                processed = true;
            } else {
                tryCount ++;
                if (tryCount <= 5) {
                    throw new RuntimeException("perfornUpdate, unable to update secret: " + result.toString());
                }
            }
        }
    }
    
    protected AWSSecretsManager buildAWSSecretsManager() {
        ClientConfiguration config = PredefinedClientConfigurations.defaultConfig();
        config.setCacheResponseMetadata(false);
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(awsRegion).withClientConfiguration(config).build();
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
    public boolean getSingleBooleanFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) {
        String booleanString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        Boolean secretBooleanValue = Boolean.valueOf(booleanString);
        return secretBooleanValue.booleanValue();
    }

    @Override
    public void updateSecretBoolean(String awsKeyName, boolean useKfsInstanceNamespace, boolean booleanValue) {
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, String.valueOf(booleanValue));
        
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
