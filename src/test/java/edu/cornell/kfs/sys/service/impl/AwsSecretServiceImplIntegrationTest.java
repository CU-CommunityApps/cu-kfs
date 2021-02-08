package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.CuSysTestConstants.MockAwsSecretServiceConstants;
import edu.cornell.kfs.sys.extension.AwsSecretServiceCacheExtension;
import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretPojo;
import edu.cornell.kfs.sys.util.RunnableForThrowType;
import net.bull.javamelody.internal.common.LOG;

@Execution(ExecutionMode.SAME_THREAD)
@AwsSecretServiceCacheExtension(awsSecretServiceField = "awsSecretServiceImpl")
class AwsSecretServiceImplIntegrationTest {
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
        awsSecretServiceImpl = new AwsSecretServiceImpl();
        awsSecretServiceImpl.setAwsRegion(MockAwsSecretServiceConstants.AWS_US_EAST_ONE_REGION);
        awsSecretServiceImpl.setKfsSharedNamespace(MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE);
        awsSecretServiceImpl.setKfsInstanceNamespace(MockAwsSecretServiceConstants.KFS_LOCALDEV_INSTANCE_NAMESPACE);
        awsSecretServiceImpl.setRetryCount(MockAwsSecretServiceConstants.AWS_SECRET_DEFAULT_UPDATE_RETRY_COUNT);
        awsSecretServiceImpl.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws Exception {
        awsSecretServiceImpl = null;
    }

    @Test
    void testGetSingleStringValueFromAwsSecret() throws Exception {
        String actualSecretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(SINGLE_STRING_SECRET_KEY_NAME, false);
        assertEquals(SINGLE_STRING_SECRET_VALUE, actualSecretValue);
    }
    
    @Test
    void testDateSetAndGet() throws Exception {
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        awsSecretServiceImpl.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, false, date);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            Date secretDate = awsSecretServiceImpl.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME, false);
            assertEquals(date, secretDate);
        });
    }
    
    @Test
    void testBooleanSetAndGet() throws Exception {
        boolean initialValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
        boolean expectedNewBoolean = !initialValue;
        awsSecretServiceImpl.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, false, expectedNewBoolean);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            boolean actualNewBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                    SINGLE_BOOLEAN_SECRET_KEY_NAME, false);
            assertEquals(expectedNewBoolean, actualNewBoolean);
        });
    }
    
    @Test
    void testPojo() throws Exception {
        String newUniqueString = UUID.randomUUID().toString();
        Date newDate = new Date(Calendar.getInstance().getTimeInMillis());
        
        AwsSecretPojo pojo = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
        LOG.info("testPojo, pojo: " + pojo);
        pojo.setChangeable_string(newUniqueString);
        pojo.setUpdate_date(newDate);
        boolean newBooleanTest = !pojo.isBoolean_test();
        pojo.setBoolean_test(newBooleanTest);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, pojo);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            AwsSecretPojo pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(
                    BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class);
            assertEquals(newUniqueString, pojoNew.getChangeable_string());
            assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
            assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
            assertEquals(pojo.getUpdate_date(), pojoNew.getUpdate_date());
            assertEquals(pojo.isBoolean_test(), pojoNew.isBoolean_test());
        });
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
    void testNumber() throws Exception {
        Random rand = new Random();
        float floatNumber = rand.nextFloat();
        awsSecretServiceImpl.updateSecretNumber(SINGLE_FLOAT_SECRET_KEY_NAME, true, floatNumber);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            float returnedNumber = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(
                    SINGLE_FLOAT_SECRET_KEY_NAME, true);
            assertEquals(floatNumber, returnedNumber);
        });
    }

    private void performTaskBeforeAndAfterClearingAwsCache(RunnableForThrowType<Exception> task) throws Exception {
        task.run();
        awsSecretServiceImpl.removeAllSecretsFromCurrentCache();
        task.run();
    }

}
