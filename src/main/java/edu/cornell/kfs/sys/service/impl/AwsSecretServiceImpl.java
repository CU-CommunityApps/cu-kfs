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
    
    private Map<Class, Map<String, String>> awsSecretsCache;
    
    @Override
    public void initializeCache(Class cacheScope) {
        if (awsSecretsCache == null) {
            awsSecretsCache = new HashMap<Class, Map<String, String>>();
        }
        awsSecretsCache.put(cacheScope, new HashMap<String, String>());
        logCacheStatus();
    }
    
    @Override
    public void clearCache(Class cacheScope) {
        awsSecretsCache.put(cacheScope, new HashMap<String, String>());
        logCacheStatus();
    }
    
    public void logCacheStatus() {
        if (LOG.isDebugEnabled()) {
            if (awsSecretsCache == null) {
                LOG.debug("logCacheStatus, the cache has not been set");
            } else if (awsSecretsCache.isEmpty()) {
                LOG.debug("logCacheStatus, the cache has been set, but is empty");
            } else {
                for (Class cacheScopeClass : awsSecretsCache.keySet()) {
                    Map<String, String> cacheScope = awsSecretsCache.get(cacheScopeClass);
                    if (cacheScope.keySet().isEmpty()) {
                    } else {
                        for (String key : cacheScope.keySet()) {
                            LOG.debug("logCacheStatus, cache scope: " + cacheScopeClass + " AWS secret key: " + key);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String getSingleStringValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace) {
        validateCacheSet(cacheScope);
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        String cachedSecretValue = retrieveSecretFromCache(cacheScope, fullAwsKey);
        
        if (StringUtils.isNotBlank(cachedSecretValue)) {
            return cachedSecretValue;
        } else {
            LOG.debug("getSingleStringValueFromAwsSecret, in cache " + cacheScope + " there was no value for " + fullAwsKey);
            String secretValue = retrieveSecretFromAws(fullAwsKey);
            updateCacheValue(cacheScope, fullAwsKey, secretValue);
            return secretValue;
        }
    }

    private void validateCacheSet(Class cacheScope) {
        if (awsSecretsCache == null || !awsSecretsCache.containsKey(cacheScope)) {
            throw new RuntimeException("The AWS secret cache has not been initialized for " + cacheScope);
        }
    }
    
    private String retrieveSecretFromCache(Class cacheScope, String fullAwsKey) {
        Map<String, String> cache = awsSecretsCache.get(cacheScope);
        if (cache.containsKey(fullAwsKey)) {
            LOG.debug("retrieveSecretFromCache, in cache " + cacheScope + " found a secret value for " + fullAwsKey);
            return cache.get(fullAwsKey);
        } else {
            return null;
        }
    }
    
    private void updateCacheValue(Class cacheScope, String fullAwsKey, String secretValue) {
        LOG.debug("updateCacheValue, in cache " + cacheScope + " setting value for secret key " + fullAwsKey);
        Map<String, String> cache = awsSecretsCache.get(cacheScope);
        cache.put(fullAwsKey, secretValue);
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
    public void updateSecretValue(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        validateCacheSet(cacheScope);
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest ().withSecretId(fullAwsKey);
        updateSecretRequest.setSecretString(keyValue);
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            performUpdate(updateSecretRequest, client);
            updateCacheValue(cacheScope, fullAwsKey, keyValue);
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
    public Date getSingleDateValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException {
        String dateString = getSingleStringValueFromAwsSecret(cacheScope, awsKeyName, useKfsInstanceNamespace);
        return convertStringToDate(dateString);
    }
    
    @Override
    public void updateSecretDate(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Date date) {
        updateSecretValue(cacheScope, awsKeyName, useKfsInstanceNamespace, convertDateToString(date));
    }
    
    @Override
    public boolean getSingleBooleanFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace) {
        String booleanString = getSingleStringValueFromAwsSecret(cacheScope, awsKeyName, useKfsInstanceNamespace);
        Boolean secretBooleanValue = Boolean.valueOf(booleanString);
        return secretBooleanValue.booleanValue();
    }

    @Override
    public void updateSecretBoolean(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, boolean booleanValue) {
        updateSecretValue(cacheScope, awsKeyName, useKfsInstanceNamespace, String.valueOf(booleanValue));
        
    }
    
    @Override
    public float getSingleNumberValueFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace) {
        String floatString = getSingleStringValueFromAwsSecret(cacheScope, awsKeyName, useKfsInstanceNamespace);
        return Float.valueOf(floatString);
    }
    
    @Override
    public void updateSecretNumber(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace,  float numericValue) {
        updateSecretValue(cacheScope, awsKeyName, useKfsInstanceNamespace, String.valueOf(numericValue));
    }

    @Override
    public <T> T getPojoFromAwsSecret(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) 
            throws JsonMappingException, JsonProcessingException {
        String pojoJsonString = getSingleStringValueFromAwsSecret(cacheScope, awsKeyName, useKfsInstanceNamespace);
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        T object = objectMapper.readValue(pojoJsonString, objectType);
        return object;
    }
    
    @Override
    public void updatePojo(Class cacheScope, String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) throws JsonProcessingException {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        String jsonString = objectMapper.writeValueAsString(pojo);
        updateSecretValue(cacheScope, awsKeyName, useKfsInstanceNamespace, jsonString);
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
