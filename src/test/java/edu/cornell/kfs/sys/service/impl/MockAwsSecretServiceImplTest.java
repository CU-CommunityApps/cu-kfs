package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CuSysTestConstants.MockAwsSecretServiceConstants;
import edu.cornell.kfs.sys.extension.AwsSecretServiceCacheExtension;
import edu.cornell.kfs.sys.extension.ExcludeAwsSecretsCacheSetup;
import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretPojo;
import edu.cornell.kfs.sys.util.ConsumerForThrowType;
import edu.cornell.kfs.sys.util.CuJsonTestUtils;
import edu.cornell.kfs.sys.util.RunnableForThrowType;

/**
 * This is a modified copy of the cu-kfs AwsSecretServiceImplIntegrationTest class,
 * which has been changed to validate the Mock AWS Secret Service instead.
 */
@Execution(ExecutionMode.SAME_THREAD)
@AwsSecretServiceCacheExtension(awsSecretServiceField = "awsSecretServiceImpl")
public class MockAwsSecretServiceImplTest {

    private static final Logger LOG = LogManager.getLogger();

    private static final String SINGLE_STRING_SECRET_KEY_NAME = "unittest/singlestring";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "unittest/singledate";
    private static final String BASIC_POJO_SECRET_KEY_NAME = "unittest/pojo";
    private static final String SINGLE_BOOLEAN_SECRET_KEY_NAME = "unittest/singleboolean";
    private static final String SINGLE_FLOAT_SECRET_KEY_NAME = "unittest/singlefloat";
    private static final String NONEXISTENT_SECRET_KEY_NAME = "unittest/nonexistent";
    
    private static final String STATIC_STRING_PROPERTY_NAME = "static_string";
    private static final String NUMBER_TEST_PROPERTY_NAME = "number_test";
    
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String SINGLE_STRING_SECRET_ALT_VALUE = "Alternate Val";
    private static final Boolean SINGLE_BOOLEAN_SECRET_VALUE = Boolean.TRUE;
    private static final String BASIC_POJO_STATIC_STRING_VALUE = "do not change me";
    private static final int BASIC_POJO_NUMBER_VALUE = 1;
    private static final float SINGLE_FLOAT_SECRET_VALUE = 9.75f;
    private static final String SINGLE_DATE_SECRET_VALUE = "2020-01-31T11:30:55.123";
    
    private MockAwsSecretServiceImpl awsSecretServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        awsSecretServiceImpl = new MockAwsSecretServiceImpl();
        awsSecretServiceImpl.overrideLocalSecrets(
                buildSharedSecret(SINGLE_STRING_SECRET_KEY_NAME, SINGLE_STRING_SECRET_VALUE),
                buildInstanceSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, SINGLE_BOOLEAN_SECRET_VALUE.toString()),
                buildSharedSecret(BASIC_POJO_SECRET_KEY_NAME, buildInitialPojoJson()),
                buildSharedSecret(SINGLE_DATE_SECRET_KEY_NAME, SINGLE_DATE_SECRET_VALUE),
                buildInstanceSecret(SINGLE_FLOAT_SECRET_KEY_NAME, String.valueOf(SINGLE_FLOAT_SECRET_VALUE)));
    }

    private Map.Entry<String, String> buildSharedSecret(String awsKeyName, String value) {
        return Map.entry(MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE + awsKeyName, value);
    }

    private Map.Entry<String, String> buildInstanceSecret(String awsKeyName, String value) {
        return Map.entry(MockAwsSecretServiceConstants.KFS_LOCALDEV_INSTANCE_NAMESPACE + awsKeyName, value);
    }

    private String buildInitialPojoJson() {
        return CuJsonTestUtils.buildJsonStringFromEntries(
                Map.entry(STATIC_STRING_PROPERTY_NAME, BASIC_POJO_STATIC_STRING_VALUE),
                Map.entry(NUMBER_TEST_PROPERTY_NAME, String.valueOf(BASIC_POJO_NUMBER_VALUE)));
    }

    @AfterEach
    void tearDown() throws Exception {
        awsSecretServiceImpl = null;
    }

    @Test
    void testGetSingleStringValueFromAwsSecret() throws Exception {
        String actualSecretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                SINGLE_STRING_SECRET_KEY_NAME, false);
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
        boolean initialValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
        boolean expectedNewBoolean = !initialValue;
        awsSecretServiceImpl.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, true, expectedNewBoolean);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            boolean actualNewBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                    SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
            assertEquals(expectedNewBoolean, actualNewBoolean);
        });
    }
    
    @Test
    void testPojo() throws Exception {
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { KFSConstants.BLANK_SPACE })
    void testReadAndWriteStringAwsSecretWithBlankValue(String blankValue) throws Exception {
        awsSecretServiceImpl.updateSecretValue(SINGLE_STRING_SECRET_KEY_NAME, false, blankValue);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            String awsString = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertEquals(StringUtils.EMPTY, awsString, "Retrieved string should have been empty");
        });
        
        String storedAwsStringValue = awsSecretServiceImpl.getLocalSecretValue(SINGLE_STRING_SECRET_KEY_NAME, false);
        assertEquals(CUKFSConstants.NULL_AWS_SECRET_VALUE, storedAwsStringValue,
                "Wrong back-end value for null/blank string");
    }

    @Test
    void testReadPrimitiveAwsSecretsWithInitialNullValues() throws Exception {
        String fullBooleanAwsKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
        String fullFloatAwsKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_FLOAT_SECRET_KEY_NAME, true);
        awsSecretServiceImpl.overrideLocalSecrets(
                Map.entry(fullBooleanAwsKey, CUKFSConstants.NULL_AWS_SECRET_VALUE),
                Map.entry(fullFloatAwsKey, CUKFSConstants.NULL_AWS_SECRET_VALUE));
        
        boolean awsBoolean = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
        float awsFloat = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(SINGLE_FLOAT_SECRET_KEY_NAME, true);
        
        assertEquals(false, awsBoolean, "A null AWS secret boolean should have defaulted to false");
        assertEquals(0f, awsFloat, "A null AWS secret number should have defaulted to zero");
        
        String storedAwsBooleanValue = awsSecretServiceImpl.getLocalSecretValue(fullBooleanAwsKey);
        String storedAwsFloatValue = awsSecretServiceImpl.getLocalSecretValue(fullFloatAwsKey);
        
        assertEquals(CUKFSConstants.NULL_AWS_SECRET_VALUE, storedAwsBooleanValue,
                "Wrong back-end value for null boolean");
        assertEquals(CUKFSConstants.NULL_AWS_SECRET_VALUE, storedAwsFloatValue,
                "Wrong back-end value for null number");
    }

    @Test
    void testReadAndWriteDateAndPojoAwsSecretsWithNullValues() throws Exception {
        awsSecretServiceImpl.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, false, null);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, null);
        
        performTaskBeforeAndAfterClearingAwsCache(() -> {
            Date awsDate = awsSecretServiceImpl.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME, false);
            AwsSecretPojo awsPojo = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false,
                    AwsSecretPojo.class);
        
            assertNull(awsDate, "Retrieved Date should have been null");
            assertNull(awsPojo, "Retrieved POJO should have been null");
            
            awsSecretServiceImpl.removeAllSecretsFromCurrentCache();
        });
        
        String storedAwsDateValue = awsSecretServiceImpl.getLocalSecretValue(SINGLE_DATE_SECRET_KEY_NAME, false);
        String storedAwsPojoValue = awsSecretServiceImpl.getLocalSecretValue(BASIC_POJO_SECRET_KEY_NAME, false);
        
        assertEquals(CUKFSConstants.NULL_AWS_SECRET_VALUE, storedAwsDateValue, "Wrong back-end value for null Date");
        assertEquals(CUKFSConstants.NULL_AWS_SECRET_VALUE, storedAwsPojoValue, "Wrong back-end value for null POJO");
    }

    static Stream<ConsumerForThrowType<MockAwsSecretServiceImpl, Exception>>
            serviceCallsReferencingSecretsThatDoNotExist() {
        return Stream.of(
                service -> service.getSingleStringValueFromAwsSecret(NONEXISTENT_SECRET_KEY_NAME, false),
                service -> service.getSingleStringValueFromAwsSecret(NONEXISTENT_SECRET_KEY_NAME, true),
                service -> service.getSingleStringValueFromAwsSecret(SINGLE_STRING_SECRET_KEY_NAME, true),
                service -> service.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, false),
                service -> service.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME, true),
                service -> service.getSingleNumberValueFromAwsSecret(SINGLE_FLOAT_SECRET_KEY_NAME, false),
                service -> service.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, true, AwsSecretPojo.class),
                service -> service.updateSecretValue(NONEXISTENT_SECRET_KEY_NAME, false, SINGLE_STRING_SECRET_VALUE),
                service -> service.updateSecretValue(NONEXISTENT_SECRET_KEY_NAME, true, SINGLE_STRING_SECRET_VALUE),
                service -> service.updateSecretValue(
                        SINGLE_STRING_SECRET_KEY_NAME, true, SINGLE_STRING_SECRET_ALT_VALUE),
                service -> service.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, false, false),
                service -> service.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, true, new Date()),
                service -> service.updateSecretNumber(SINGLE_FLOAT_SECRET_KEY_NAME, false, 1.0f),
                service -> service.updatePojo(BASIC_POJO_SECRET_KEY_NAME, true, new AwsSecretPojo())
        );
    }

    @ParameterizedTest
    @MethodSource("serviceCallsReferencingSecretsThatDoNotExist")
    void testCannotReadOrWriteAwsSecretsThatDoNotExist(
            ConsumerForThrowType<MockAwsSecretServiceImpl, Exception> awsSecretServiceAction) throws Exception {
        assertThrows(RuntimeException.class, () -> awsSecretServiceAction.accept(awsSecretServiceImpl),
                "The AWS Secret read/write operation should have failed when it references "
                        + "an AWS secret that does not exist under the given name and namespace");
    }

    static Stream<ConsumerForThrowType<MockAwsSecretServiceImpl, Exception>>
            serviceCallsExpectedToFailOutsideOfHelperServiceMethod() {
        return Stream.of(
                service -> service.getSingleStringValueFromAwsSecret(SINGLE_STRING_SECRET_KEY_NAME, false),
                service -> service.getSingleBooleanFromAwsSecret(SINGLE_BOOLEAN_SECRET_KEY_NAME, true),
                service -> service.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME, false),
                service -> service.getSingleNumberValueFromAwsSecret(SINGLE_FLOAT_SECRET_KEY_NAME, true),
                service -> service.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretPojo.class),
                service -> service.updateSecretValue(
                        SINGLE_STRING_SECRET_KEY_NAME, false, SINGLE_STRING_SECRET_ALT_VALUE),
                service -> service.updateSecretBoolean(SINGLE_BOOLEAN_SECRET_KEY_NAME, true, false),
                service -> service.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, false, new Date()),
                service -> service.updateSecretNumber(SINGLE_FLOAT_SECRET_KEY_NAME, true, 1.0f),
                service -> service.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, new AwsSecretPojo()),
                service -> service.removeSecretFromCurrentCache(SINGLE_STRING_SECRET_KEY_NAME, false),
                service -> service.removeAllSecretsFromCurrentCache()
        );
    }

    @ParameterizedTest
    @MethodSource("serviceCallsExpectedToFailOutsideOfHelperServiceMethod")
    @ExcludeAwsSecretsCacheSetup
    void testCannotUseMostAwsSecretServiceCallsOutsideOfHelperServiceMethod(
            ConsumerForThrowType<MockAwsSecretServiceImpl, Exception> awsSecretServiceAction) throws Exception {
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        assertThrows(IllegalStateException.class, () -> awsSecretServiceAction.accept(awsSecretServiceImpl),
                "The AWS Secret service operation should have failed when it was executed "
                        + "outside of a performTaskRequiringAccessToAwsSecrets() method call");
    }

    @Test
    @ExcludeAwsSecretsCacheSetup
    void testBasicHandlingAndPrecedenceOfAwsSecretsCache() throws Exception {
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        
        String singleStringValue = awsSecretServiceImpl.performTaskRequiringAccessToAwsSecrets(() -> {
            assertSecretsCacheExistsAndIsEmpty();
            
            String fullAwsKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_STRING_SECRET_KEY_NAME, false);
            String secretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsKey, SINGLE_STRING_SECRET_VALUE, secretValue);
            
            awsSecretServiceImpl.overrideLocalSecrets(Map.entry(fullAwsKey, SINGLE_STRING_SECRET_ALT_VALUE));
            String secretValue2 = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertEquals(SINGLE_STRING_SECRET_VALUE, secretValue2,
                    "Wrong value of string secret; the cached value should have been returned");
            assertSecretsCacheContainsValue(fullAwsKey, SINGLE_STRING_SECRET_VALUE);
            
            return secretValue;
        });
        
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        assertEquals(SINGLE_STRING_SECRET_VALUE, singleStringValue, "Wrong value returned from Callable");
    }

    @Test
    @ExcludeAwsSecretsCacheSetup
    void testAwsSecretsCacheHandlingOfUpdates() throws Exception {
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        
        String singleStringValue = awsSecretServiceImpl.performTaskRequiringAccessToAwsSecrets(() -> {
            assertSecretsCacheExistsAndIsEmpty();
            
            String fullAwsKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_STRING_SECRET_KEY_NAME, false);
            awsSecretServiceImpl.updateSecretValue(
                    SINGLE_STRING_SECRET_KEY_NAME, false, SINGLE_STRING_SECRET_ALT_VALUE);
            assertSecretsCacheContainsValue(fullAwsKey, SINGLE_STRING_SECRET_ALT_VALUE);
            
            String secretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsKey, SINGLE_STRING_SECRET_ALT_VALUE, secretValue);
            
            return secretValue;
        });
        
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        assertEquals(SINGLE_STRING_SECRET_ALT_VALUE, singleStringValue, "Wrong value returned from Callable");
    }

    @Test
    @ExcludeAwsSecretsCacheSetup
    void testManuallyClearCachedSecrets() throws Exception {
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        
        Boolean singleBooleanValue = awsSecretServiceImpl.performTaskRequiringAccessToAwsSecrets(() -> {
            assertSecretsCacheExistsAndIsEmpty();
            
            String fullAwsStringKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_STRING_SECRET_KEY_NAME, false);
            String stringValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsStringKey, SINGLE_STRING_SECRET_VALUE, stringValue);
            
            awsSecretServiceImpl.removeSecretFromCurrentCache(SINGLE_STRING_SECRET_KEY_NAME, false);
            assertSecretsCacheExistsAndIsEmpty();
            
            String fullAwsBooleanKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
            boolean booleanValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                    SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
            stringValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                    SINGLE_STRING_SECRET_KEY_NAME, false);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsBooleanKey, SINGLE_BOOLEAN_SECRET_VALUE, booleanValue);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsStringKey, SINGLE_STRING_SECRET_VALUE, stringValue);
            
            for (int i = 0; i < 2; i++) {
                awsSecretServiceImpl.removeSecretFromCurrentCache(SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
                assertSecretsCacheDoesNotContainMapping(fullAwsBooleanKey);
                assertSecretsCacheContainsValue(fullAwsStringKey, SINGLE_STRING_SECRET_VALUE);
            }
            
            booleanValue = awsSecretServiceImpl.getSingleBooleanFromAwsSecret(
                    SINGLE_BOOLEAN_SECRET_KEY_NAME, true);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsBooleanKey, SINGLE_BOOLEAN_SECRET_VALUE, booleanValue);
            assertSecretsCacheContainsValue(fullAwsStringKey, SINGLE_STRING_SECRET_VALUE);
            
            for (int i = 0; i < 2; i++) {
                awsSecretServiceImpl.removeAllSecretsFromCurrentCache();
                assertSecretsCacheExistsAndIsEmpty();
            }
            
            return booleanValue;
        });
        
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        assertEquals(SINGLE_BOOLEAN_SECRET_VALUE, singleBooleanValue, "Wrong value returned from Callable");
    }

    @Test
    @ExcludeAwsSecretsCacheSetup
    void testNestedSecretsCaches() throws Exception {
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        
        Float singleFloatValue = awsSecretServiceImpl.performTaskRequiringAccessToAwsSecrets(() -> {
            assertSecretsCacheExistsAndIsEmpty();
            
            String fullAwsStringKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_STRING_SECRET_KEY_NAME, false);
            String fullAwsFloatKey = awsSecretServiceImpl.buildFullAwsKeyName(SINGLE_FLOAT_SECRET_KEY_NAME, true);
            float floatValue = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(
                    SINGLE_FLOAT_SECRET_KEY_NAME, true);
            assertReturnedSecretAndCachedSecretHaveValue(fullAwsFloatKey, SINGLE_FLOAT_SECRET_VALUE, floatValue);
            assertSecretsCacheDoesNotContainMapping(fullAwsStringKey);
            
            float floatValueDuplicate = awsSecretServiceImpl.performTaskRequiringAccessToAwsSecrets(() -> {
                assertSecretsCacheExistsAndIsEmpty();
                
                float floatValue2 = awsSecretServiceImpl.getSingleNumberValueFromAwsSecret(
                        SINGLE_FLOAT_SECRET_KEY_NAME, true);
                String stringValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(
                        SINGLE_STRING_SECRET_KEY_NAME, false);
                assertReturnedSecretAndCachedSecretHaveValue(fullAwsFloatKey, SINGLE_FLOAT_SECRET_VALUE, floatValue2);
                assertReturnedSecretAndCachedSecretHaveValue(
                        fullAwsStringKey, SINGLE_STRING_SECRET_VALUE, stringValue);
                
                return floatValue2;
            });
            
            assertEquals(SINGLE_FLOAT_SECRET_VALUE, floatValueDuplicate, "Wrong value returned from nested Callable");
            assertSecretsCacheContainsValue(fullAwsFloatKey, String.valueOf(SINGLE_FLOAT_SECRET_VALUE));
            assertSecretsCacheDoesNotContainMapping(fullAwsStringKey);
            
            return floatValue;
        });
        
        assertCannotUseSecretsCacheOutsideOfHelperServiceMethod();
        assertEquals(SINGLE_FLOAT_SECRET_VALUE, singleFloatValue, "Wrong value returned from Callable");
    }

    private void assertCannotUseSecretsCacheOutsideOfHelperServiceMethod() {
        assertThrows(IllegalStateException.class, awsSecretServiceImpl::getCurrentAwsSecretsCache,
                "Internal AWS secrets cache should not be usable outside of a "
                        + "performTaskRequiringAccessToAwsSecrets() call");
    }

    private void assertSecretsCacheExistsAndIsEmpty() throws Exception {
        Map<String, String> secretsCache = awsSecretServiceImpl.getCurrentAwsSecretsCache();
        assertNotNull(secretsCache, "Internal AWS secrets cache should have been non-null");
        assertTrue(secretsCache.isEmpty(), "Internal AWS secrets cache should have been empty");
    }

    private <T> void assertReturnedSecretAndCachedSecretHaveValue(
            String fullAwsKey, T expectedValue, T actualValue) {
        assertEquals(expectedValue, actualValue, "Wrong value of secret for key " + fullAwsKey);
        assertSecretsCacheContainsValue(fullAwsKey, expectedValue.toString());
    }

    private void assertSecretsCacheContainsValue(String fullAwsKey, String expectedCachedValue) {
        Map<String, String> secretsCache = awsSecretServiceImpl.getCurrentAwsSecretsCache();
        String actualCachedValue = secretsCache.get(fullAwsKey);
        assertEquals(expectedCachedValue, actualCachedValue, "Wrong value of cached secret for key " + fullAwsKey);
    }

    private void assertSecretsCacheDoesNotContainMapping(String fullAwsKey) {
        Map<String, String> secretsCache = awsSecretServiceImpl.getCurrentAwsSecretsCache();
        assertFalse(secretsCache.containsKey(fullAwsKey),
                "Internal AWS secrets cache should not have contained a mapping for key: " + fullAwsKey);
    }

    private void performTaskBeforeAndAfterClearingAwsCache(RunnableForThrowType<Exception> task) throws Exception {
        task.run();
        awsSecretServiceImpl.removeAllSecretsFromCurrentCache();
        task.run();
    }

}
