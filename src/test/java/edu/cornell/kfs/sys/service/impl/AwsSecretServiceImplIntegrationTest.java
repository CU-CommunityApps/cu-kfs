package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretPojo;

class AwsSecretServiceImplIntegrationTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String AWS_US_EAST_ONE_REGION = "us-east-1";
    private static final String KFS_INSTANCE_NAMESPACE = "kfs/local-dev/";
    private static final String KFS_SHARED_NAMESPACE = "kfs/kfs-shared/";
    private static final int AWS_SECRET_UPDATE_RETRY_COUNT = 5;

    private static final String SINGLE_STRING_SECRET_KEY_NAME = "unittest/singlestring";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "unittest/singledate";
    private static final String BASIC_POJO_SECRET_KEY_NAME = "unittest/pojo";
    private static final String SINGLE_BOOLEAN_SECRET_KEY_NAME = "unittest/singleboolean";
    private static final String SINGLE_FLOAT_SECRET_KEY_NAME = "unittest/singlefloat";
    
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String BASIC_POJO_STATIC_STRING_VALUE = "do not change me";
    private static final int BASIC_POJO_NUMBER_VALUE = 1;
    
    
    private AwsSecretServiceImpl awsSecretServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        org.apache.log4j.LogManager.getLogger(AwsSecretServiceImpl.class).setLevel(org.apache.log4j.Level.DEBUG);
        
        /* 
         * This logging was helpful in debugging things.  we don't want this logging long term, but it would be helpful to 
         * keep them to be easily recovered
         * 
        org.apache.log4j.LogManager.getLogger("software.amazon.awssdk").setLevel(org.apache.log4j.Level.DEBUG);
        org.apache.log4j.LogManager.getLogger("software.amazon.awssdk.request").setLevel(org.apache.log4j.Level.DEBUG);
        org.apache.log4j.LogManager.getLogger("org.apache.http.wire").setLevel(org.apache.log4j.Level.DEBUG);
        */
        
        awsSecretServiceImpl = new AwsSecretServiceImpl();
        awsSecretServiceImpl.setAwsRegion(AWS_US_EAST_ONE_REGION);
        awsSecretServiceImpl.setKfsSharedNamespace(KFS_SHARED_NAMESPACE);
        awsSecretServiceImpl.setKfsInstanceNamespace(KFS_INSTANCE_NAMESPACE);
        awsSecretServiceImpl.setRetryCount(AWS_SECRET_UPDATE_RETRY_COUNT);
    }

    @AfterEach
    void tearDown() throws Exception {
        awsSecretServiceImpl = null;
    }

    @Test
    void testGetSingleStringValueFromAwsSecret() {
        String actualSecretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(SINGLE_STRING_SECRET_KEY_NAME, false);
        assertEquals(SINGLE_STRING_SECRET_VALUE, actualSecretValue);
    }
    
    @Test
    void testDateSetAndGet() throws ParseException {
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        awsSecretServiceImpl.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, false, date);
        
        Date secretDate = awsSecretServiceImpl.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME, false);
        assertEquals(date, secretDate);
    }
    
    @Test
    void testBooleanSetAndGetWithCache() {
        boolean initialValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        boolean expectedNewBoolean = !initialValue;
        awsSecretServiceImpl.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, false, expectedNewBoolean);
        
        boolean actualNewBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        
        assertEquals(expectedNewBoolean, actualNewBoolean);
    }
    
    @Test
    void testBooleanSetAndGetWithOutCache() {
        boolean initialValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        boolean expectedNewBoolean = !initialValue;
        awsSecretServiceImpl.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, false, expectedNewBoolean);
        awsSecretServiceImpl.clearCache();
        
        waitAfterUpdatingSecretBeforeGettingSecret();
        
        boolean actualNewBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        
        assertEquals(expectedNewBoolean, actualNewBoolean);
    }

    protected void waitAfterUpdatingSecretBeforeGettingSecret() {
        LOG.info("waitAfterUpdatingSecretBeforeGettingSecret, waiting 5 seconds");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            LOG.error("waitAfterUpdatingSecretBeforeGettingSecret. had an error waiting", e);
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void testPojoWithCache() throws JsonMappingException, JsonProcessingException {
        String newUniqueString = UUID.randomUUID().toString();
        Date newDate = new Date(Calendar.getInstance(Locale.US).getTimeInMillis());
        
        AwsSecretPojo pojo = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        LOG.info("testPojoWithCache, pojo: " + pojo);
        pojo.setChangeable_string(newUniqueString);
        pojo.setUpdate_date(newDate);
        boolean newBooleanTest = !pojo.isBoolean_test();
        pojo.setBoolean_test(newBooleanTest);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, pojo);
        
        AwsSecretPojo pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        assertEquals(newUniqueString, pojoNew.getChangeable_string());
        assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
        assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
        assertEquals(pojo.getUpdate_date(), pojoNew.getUpdate_date());
        assertEquals(pojo.isBoolean_test(), pojoNew.isBoolean_test());
    }
    
    @Test
    void testPojoWithOutCache() throws JsonMappingException, JsonProcessingException {
        String newUniqueString = UUID.randomUUID().toString();
        Date newDate = new Date(Calendar.getInstance(Locale.US).getTimeInMillis());
        
        AwsSecretPojo pojo = awsSecretServiceImpl.getPojoFromAwsSecret( BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        LOG.info("testPojoWithOutCache, pojo: " + pojo);
        pojo.setChangeable_string(newUniqueString);
        pojo.setUpdate_date(newDate);
        boolean newBooleanTest = !pojo.isBoolean_test();
        pojo.setBoolean_test(newBooleanTest);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, pojo);
        awsSecretServiceImpl.clearCache();
        
        waitAfterUpdatingSecretBeforeGettingSecret();
        
        AwsSecretPojo pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        assertEquals(newUniqueString, pojoNew.getChangeable_string());
        assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
        assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
        assertEquals(pojo.getUpdate_date(), pojoNew.getUpdate_date());
        assertEquals(pojo.isBoolean_test(), pojoNew.isBoolean_test());
        awsSecretServiceImpl.logCacheStatus();
    }
    
    @Test 
    void testBuildFullAwsKeyNameAnyNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, false);
        String expectedFullNameSpace = KFS_SHARED_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }
    
    @Test 
    void testBuildFullAwsKeyNameInstanceNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, true);
        String expectedFullNameSpace = KFS_INSTANCE_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }
    
    @Test
    void testNumber() {
        Random rand = new Random();
        float floatNumber = rand.nextFloat();
        awsSecretServiceImpl.updateSecretNumber(SINGLE_FLOAT_SECRET_KEY_NAME, true, floatNumber);
        
        float returnedNumber = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(SINGLE_FLOAT_SECRET_KEY_NAME, true);
        assertEquals(floatNumber, returnedNumber);
    }
    
    @Test
    void testRetrieveSecretFromCacheNull() {
        awsSecretServiceImpl.clearCache();
        Exception exception = assertThrows(RuntimeException.class, () -> {
            awsSecretServiceImpl.retrieveSecretFromCache(null);
        });

        assertEquals(AwsSecretServiceImpl.A_NULL_AWS_KEY_IS_NOT_ALLOWED, exception.getMessage());
    }
    
    @Test
    void testRetrieveSecretFromCacheEmpty() {
        awsSecretServiceImpl.clearCache();
        String results = awsSecretServiceImpl.retrieveSecretFromCache(StringUtils.EMPTY);
        assertTrue(StringUtils.isBlank(results));
    }

}
