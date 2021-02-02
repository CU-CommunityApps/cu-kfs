package edu.cornell.kfs.sys.service.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.cornell.kfs.sys.CuSysTestConstants.MockAwsSecretServiceConstants;
import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretPojo;
import edu.cornell.kfs.sys.util.CuJsonUtils;

/**
 * This is a modified copy of the cu-kfs AwsSecretServiceImplIntegrationTest class,
 * which has been changed to validate the Mock AWS Secret Service instead.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class MockAwsSecretServiceImplTest {

    private static final Logger LOG = LogManager.getLogger();

    private static final String SINGLE_STRING_SECRET_KEY_NAME = "unittest/singlestring";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "unittest/singledate";
    private static final String BASIC_POJO_SECRET_KEY_NAME = "unittest/pojo";
    private static final String SINGLE_BOOLEAN_SECRET_KEY_NAME = "unittest/singleboolean";
    private static final String SINGLE_FLOAT_SECRET_KEY_NAME = "unittest/singlefloat";
    
    private static final String STATIC_STRING_PROPERTY_NAME = "static_string";
    private static final String NUMBER_TEST_PROPERTY_NAME = "number_test";
    
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String BASIC_POJO_STATIC_STRING_VALUE = "do not change me";
    private static final int BASIC_POJO_NUMBER_VALUE = 1;
    
    private MockAwsSecretServiceImpl awsSecretServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        awsSecretServiceImpl = new MockAwsSecretServiceImpl();
        awsSecretServiceImpl.setInitialSecrets(
                buildSharedSecret(SINGLE_STRING_SECRET_KEY_NAME, SINGLE_STRING_SECRET_VALUE),
                buildSharedSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, Boolean.TRUE.toString()),
                buildSharedSecret(BASIC_POJO_SECRET_KEY_NAME, buildInitialPojoJson()));
    }

    private Map.Entry<String, String> buildSharedSecret(String awsKeyName, String value) {
        return Map.entry(MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE + awsKeyName, value);
    }

    private String buildInitialPojoJson() {
        return CuJsonUtils.buildJsonStringFromEntries(
                Map.entry(STATIC_STRING_PROPERTY_NAME, BASIC_POJO_STATIC_STRING_VALUE),
                Map.entry(NUMBER_TEST_PROPERTY_NAME, String.valueOf(BASIC_POJO_NUMBER_VALUE)));
    }

    @AfterEach
    void tearDown() throws Exception {
        awsSecretServiceImpl = null;
    }

    @Test
    void testGetSingleStringValueFromAwsSecret() {
        String actualSecretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                SINGLE_STRING_SECRET_KEY_NAME, false);
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
    void testBooleanSetAndGet() {
        boolean initialValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        boolean expectedNewBoolean = !initialValue;
        awsSecretServiceImpl.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, false, expectedNewBoolean);
        
        boolean actualNewBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        
        assertEquals(expectedNewBoolean, actualNewBoolean);
    }
    
    @Test
    void testPojo() throws JsonMappingException, JsonProcessingException {
        String newUniqueString = UUID.randomUUID().toString();
        Date newDate = new Date(Calendar.getInstance().getTimeInMillis());
        
        AwsSecretPojo pojo = awsSecretServiceImpl.getPojoFromAwsSecret(
                BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        LOG.info("testPojo, pojo: " + pojo);
        pojo.setChangeable_string(newUniqueString);
        pojo.setUpdate_date(newDate);
        boolean newBooleanTest = !pojo.isBoolean_test();
        pojo.setBoolean_test(newBooleanTest);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, pojo);
        
        AwsSecretPojo pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(
                BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        assertEquals(newUniqueString, pojoNew.getChangeable_string());
        assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
        assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
        assertEquals(pojo.getUpdate_date(), pojoNew.getUpdate_date());
        assertEquals(pojo.isBoolean_test(), pojoNew.isBoolean_test());
    }
    
    @Test 
    void testBuildFullAwsKeyNameAnyNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, false);
        String expectedFullNameSpace = MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }
    
    @Test 
    void testBuildFullAwsKeyNameInstanceNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, true);
        String expectedFullNameSpace = MockAwsSecretServiceConstants.KFS_LOCALDEV_INSTANCE_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }
    
    @Test
    void testNumber() {
        Random rand = new Random();
        float floatNumber = rand.nextFloat();
        awsSecretServiceImpl.updateSecretNumber(SINGLE_FLOAT_SECRET_KEY_NAME, true, floatNumber);
        
        float returnedNumber = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(
                SINGLE_FLOAT_SECRET_KEY_NAME, true);
        assertEquals(floatNumber, returnedNumber);
    }
}
