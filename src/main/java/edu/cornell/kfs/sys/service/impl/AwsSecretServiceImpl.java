package edu.cornell.kfs.sys.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.AwsSecretService;
import edu.cornell.kfs.sys.util.CUJsonUtils;

public class AwsSecretServiceImpl implements AwsSecretService {
    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImpl.class);
    
    protected String awsRegion;
    protected String kfsInstanceNamespace;
    protected String kfsSharedNamespace;
    protected int retryCount;
    
    private Map<String, String> awsSecretsCache;
    
    @Override
    public void clearCache() {
        awsSecretsCache = new HashMap<String, String>();
    }
    
    @Override
    public void logCacheStatus() {
        if (LOG.isDebugEnabled()) {
            if (awsSecretsCache == null) {
                LOG.debug("logCacheStatus, the cache has not been set");
            } else if (awsSecretsCache.isEmpty()) {
                LOG.debug("logCacheStatus, the cache has been set, but is empty");
            } else {
                for (String key : awsSecretsCache.keySet()) {
                    LOG.debug("logCacheStatus, AWS secret key: " + key);
                }
            }
        }
    }
    
    @Override
    public String getSingleStringValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) {
        validateCacheSet();
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        String cachedSecretValue = retrieveSecretFromCache(fullAwsKey);
        
        if (StringUtils.isNotBlank(cachedSecretValue)) {
            return cachedSecretValue;
        } else {
            LOG.debug("getSingleStringValueFromAwsSecret, in cache there was no value for " + fullAwsKey);
            String secretValue = retrieveSecretFromAws(fullAwsKey);
            updateCacheValue(fullAwsKey, secretValue);
            return secretValue;
        }
    }

    private void validateCacheSet() {
        if (awsSecretsCache == null) {
            LOG.debug("Cache has not been setup, so run the clear cache to create a new cache element");
            clearCache();
        }
    }
    
    private String retrieveSecretFromCache(String fullAwsKey) {
        if (awsSecretsCache.containsKey(fullAwsKey)) {
            LOG.debug("retrieveSecretFromCache, in cache found a secret value for " + fullAwsKey);
            return awsSecretsCache.get(fullAwsKey);
        } else {
            return null;
        }
    }
    
    private void updateCacheValue(String fullAwsKey, String secretValue) {
        LOG.debug("updateCacheValue, in cache setting value for secret key " + fullAwsKey);
        awsSecretsCache.put(fullAwsKey, secretValue);
    }
    
    protected String retrieveSecretFromAws(String fullAwsKey) {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(fullAwsKey);
        GetSecretValueResult getSecretValueResult = null;
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (SdkClientException e) {
            LOG.error("getSingleStringValueFromAwsSecret, had an error getting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }

        return getSecretValueResult.getSecretString();
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        validateCacheSet();
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest ().withSecretId(fullAwsKey);
        updateSecretRequest.setSecretString(keyValue);
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            performUpdate(updateSecretRequest, client);
            updateCacheValue(fullAwsKey, keyValue);
        } catch (SdkClientException e) {
            LOG.error("updateSecretValue, had an error setting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }
    }

    protected void performUpdate(UpdateSecretRequest updateSecretRequest, AWSSecretsManager client) {
        boolean processed = false;
        int tryCount = 0;
        while (!processed) {
            UpdateSecretResult result = client.updateSecret(updateSecretRequest);
            if (result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
                processed = true;
            } else {
                tryCount++;
                if (tryCount >= retryCount) {
                    throw new RuntimeException("performUpdate, unable to update secret: " + result.toString());
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
            fullKeyName = kfsInstanceNamespace + awsKeyName;
        } else {
            fullKeyName = kfsSharedNamespace + awsKeyName; 
        }
        return fullKeyName;
    }
    
    @Override
    public Date getSingleDateValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException {
        String dateString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        return convertStringToDate(dateString);
    }
    
    @Override
    public void updateSecretDate(String awsKeyName, boolean useKfsInstanceNamespace, Date date) {
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, convertDateToString(date));
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
    public float getSingleNumberValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) {
        String floatString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        return Float.valueOf(floatString);
    }
    
    @Override
    public void updateSecretNumber(String awsKeyName, boolean useKfsInstanceNamespace,  float numericValue) {
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, String.valueOf(numericValue));
    }

    @Override
    public <T> T getPojoFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) 
            throws JsonMappingException, JsonProcessingException {
        String pojoJsonString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        T object = objectMapper.readValue(pojoJsonString, objectType);
        return object;
    }
    
    @Override
    public void updatePojo(String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) throws JsonProcessingException {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        String jsonString = objectMapper.writeValueAsString(pojo);
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, jsonString);
    }
    
    public String convertDateToString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        } else {
            SimpleDateFormat format = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS, Locale.US);
            return format.format(date);
        }
    }

    public Date convertStringToDate(String dateString) throws ParseException {
        if (StringUtils.isBlank(dateString)) {
            return null;
        } else {
            SimpleDateFormat format = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS, Locale.US);
            return format.parse(dateString);
        }
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public void setKfsInstanceNamespace(String kfsInstanceNamespace) {
        this.kfsInstanceNamespace = kfsInstanceNamespace;
    }

    public void setKfsSharedNamespace(String kfsSharedNamespace) {
        this.kfsSharedNamespace = kfsSharedNamespace;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
}
