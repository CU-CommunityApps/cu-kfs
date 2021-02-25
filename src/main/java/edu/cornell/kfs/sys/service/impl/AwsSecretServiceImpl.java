package edu.cornell.kfs.sys.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import com.amazonaws.ClientConfiguration;
//import com.amazonaws.PredefinedClientConfigurations;
//import com.amazonaws.SdkClientException;
//import com.amazonaws.services.secretsmanager.AWSSecretsManager;
//import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
//import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
//import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
//import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
//import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.AwsSecretService;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretResponse;

public class AwsSecretServiceImpl implements AwsSecretService {
    protected static final String A_NULL_AWS_KEY_IS_NOT_ALLOWED = "A null AWS key is not allowed.";

    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImpl.class);
    
    protected String awsRegion;
    protected String kfsInstanceNamespace;
    protected String kfsSharedNamespace;
    protected int retryCount;
    
    private Map<String, String> awsSecretsCache;
    
    @Override
    public void clearCache() {
        awsSecretsCache = new ConcurrentHashMap<>();
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
        createCacheIfNotPresent();
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

    private void createCacheIfNotPresent() {
        if (awsSecretsCache == null) {
            LOG.debug("createCacheIfNotPresent, Cache has not been setup, so run the clear cache to create a new cache element");
            clearCache();
        }
    }
    
    protected String retrieveSecretFromCache(String fullAwsKey) {
        if (fullAwsKey == null) {
            throw new RuntimeException(A_NULL_AWS_KEY_IS_NOT_ALLOWED);
        }
        if (awsSecretsCache.containsKey(fullAwsKey)) {
            LOG.debug("retrieveSecretFromCache, in cache found a secret value for " + fullAwsKey);
            return awsSecretsCache.get(fullAwsKey);
        } else {
            return null;
        }
    }
    
    private void updateCacheValue(String fullAwsKey, String secretValue) {
        if (fullAwsKey == null) {
            LOG.debug("updateCacheValue, the AWS key provided was null, so can't cache it.");
        } else if (secretValue == null) {
            LOG.debug("updateCacheValue, a null value was provided for AWS key " + fullAwsKey + " so it can not be cached");
        } else {
            LOG.debug("updateCacheValue, in cache setting value for secret key " + fullAwsKey);
            awsSecretsCache.put(fullAwsKey, secretValue);
        }
    }
    
    protected String retrieveSecretFromAws(String fullAwsKey) {
        SecretsManagerClient client = buildSecretsManagerClient();
        
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(fullAwsKey)
                    .build();
            GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);
            return valueResponse.secretString();
        } catch (SecretsManagerException e) {
            LOG.error("getSingleStringValueFromAwsSecret, had an error getting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }
    
    protected SecretsManagerClient buildSecretsManagerClient() {
        Region region = Region.of(awsRegion);
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();
        return secretsClient;
    }
    
    @Override
    public void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue) {
        String fullAwsKey = buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
        UpdateSecretRequest updateSecretRequest = UpdateSecretRequest.builder()
                .secretId(fullAwsKey)
                .secretString(keyValue)
                .build();
        SecretsManagerClient client = buildSecretsManagerClient();
        try {
            performUpdate(updateSecretRequest, client);
            updateCacheValue(fullAwsKey, keyValue);
        } catch (SecretsManagerException e) {
            LOG.error("updateSecretValue, had an error setting value for secret " + fullAwsKey, e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }
    
    protected void performUpdate(UpdateSecretRequest updateSecretRequest, SecretsManagerClient client) {
        boolean processed = false;
        int tryCount = 0;
        while (!processed) {
            UpdateSecretResponse result = client.updateSecret(updateSecretRequest);
            if (result.sdkHttpResponse().isSuccessful()) {
                processed = true;
            } else {
                tryCount++;
                if (tryCount >= retryCount) {
                    throw new RuntimeException("performUpdate, unable to update secret: " + result.toString());
                } else {
                    LOG.info("performUpdate, try number " + tryCount + " failed trying to update secret, will try again.");
                }
            }
        }
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
