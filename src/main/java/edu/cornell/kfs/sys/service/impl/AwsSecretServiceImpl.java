package edu.cornell.kfs.sys.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;

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
import edu.cornell.kfs.sys.util.CallableForThrowType;
import edu.cornell.kfs.sys.util.CuJsonUtils;

public class AwsSecretServiceImpl implements AwsSecretService {
    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImpl.class);

    private static final ThreadLocal<LinkedList<Map<String, String>>> AWS_SECRETS_CACHE_STACK =
            ThreadLocal.withInitial(LinkedList::new);

    protected String awsRegion;
    protected String kfsInstanceNamespace;
    protected String kfsSharedNamespace;
    protected int retryCount;
    
    @Override
    public <R, T extends Throwable> R doWithAwsSecretsCachingEnabled(
            CallableForThrowType<R, T> callable) throws T {
        Map<String, String> awsSecretsCache = new HashMap<>();
        boolean pushedCacheToStack = false;
        try {
            getAwsSecretsCacheStack().offerFirst(awsSecretsCache);
            pushedCacheToStack = true;
            return callable.call();
        } finally {
            try {
                awsSecretsCache.clear();
                if (pushedCacheToStack) {
                    getAwsSecretsCacheStack().pollFirst();
                }
            } catch (RuntimeException e) {
                LOG.error("doWithAwsSecretsCachingEnabled: Unexpected error when clearing and removing cache", e);
            }
        }
    }
    
    protected LinkedList<Map<String, String>> getAwsSecretsCacheStack() {
        return AWS_SECRETS_CACHE_STACK.get();
    }
    
    @Override
    public String getSingleStringValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        Map<String, String> awsSecretsCache = getCurrentAwsSecretsCache();
        return awsSecretsCache.computeIfAbsent(fullAwsKey, this::getUncachedSingleStringValueFromAwsSecret);
    }
    
    protected String getUncachedSingleStringValueFromAwsSecret(String fullAwsKey) {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(fullAwsKey);
        GetSecretValueResult getSecretValueResult = null;
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (SdkClientException e) {
            LOG.error("getUncachedSingleStringValueFromAwsSecret, had an error getting value for secret "
                    + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.shutdown();
        }

        return getSecretValueResult.getSecretString();
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        if (StringUtils.isBlank(keyValue)) {
            throw new IllegalArgumentException("keyValue to be stored in AWS Secrets cannot be blank");
        }
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        Map<String, String> awsSecretsCache = getCurrentAwsSecretsCache();
        UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest().withSecretId(fullAwsKey);
        updateSecretRequest.setSecretString(keyValue);
        
        AWSSecretsManager client = buildAWSSecretsManager();
        try {
            performUpdate(updateSecretRequest, client);
            awsSecretsCache.put(fullAwsKey, keyValue);
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
    
    protected Map<String, String> getCurrentAwsSecretsCache() {
        Map<String, String> awsSecretsCache = getAwsSecretsCacheStack().peekFirst();
        if (awsSecretsCache == null) {
            throw new IllegalStateException("AWS secrets caching has not been initialized properly; "
                    + "please call the doWithAwsSecretsCachingEnabled() method to set up the cache in advance");
        }
        return awsSecretsCache;
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
    public <T> T getPojoFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType) throws JsonMappingException, JsonProcessingException {
        String pojoJsonString = getSingleStringValueFromAwsSecret(awsKeyName, useKfsInstanceNamespace);
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        T object = objectMapper.readValue(pojoJsonString, objectType);
        return object;
    }
    
    @Override
    public void updatePojo(String awsKeyName, boolean useKfsInstanceNamespace, Object pojo) throws JsonProcessingException {
        if (ObjectUtils.isNull(pojo)) {
            throw new IllegalArgumentException("Pojo to be stored in AWS Secrets cannot be null");
        }
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        String jsonString = objectMapper.writeValueAsString(pojo);
        updateSecretValue(awsKeyName, useKfsInstanceNamespace, jsonString);
    }
    
    @Override
    public void removeSecretFromCurrentCache(String awsKeyName, boolean useKfsInstanceNamespace) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        Map<String, String> awsSecretsCache = getCurrentAwsSecretsCache();
        awsSecretsCache.remove(fullAwsKey);
    }
    
    @Override
    public void removeAllSecretsFromCurrentCache() {
        Map<String, String> awsSecretsCache = getCurrentAwsSecretsCache();
        awsSecretsCache.clear();
    }
    
    protected String convertDateToString(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date value to be stored in AWS Secrets cannot be null");
        } else {
            SimpleDateFormat format = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS, Locale.US);
            return format.format(date);
        }
    }

    protected Date convertStringToDate(String dateString) throws ParseException {
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
