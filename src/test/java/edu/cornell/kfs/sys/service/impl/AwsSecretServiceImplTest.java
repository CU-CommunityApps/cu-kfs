package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretePojoBasic;

class AwsSecretServiceImplTest {
    private static final String KFS_INSTANCE_NAMESPACE = "kfs/local-dev/";
    private static final String KFS_SHARED_NAMESPACE = "kfs/kfs-shared/";

    private static final String SINGLE_STRING_SECRET_KEY_NAME = "unittest/singlestring";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "unittest/singledate";
    private static final String BASIC_POJO_SECRET_KEY_NAME = "unittest/pojo";
    
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String BASIC_POJO_STATIC_STRING_VALUE = "do not change me";
    private static final int BASIC_POJO_NUMBER_VALUE = 1;
    
    
    private AwsSecretServiceImpl awsSecretServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        awsSecretServiceImpl = new AwsSecretServiceImpl();
        awsSecretServiceImpl.setAwsRegion("us-east-1");
        awsSecretServiceImpl.setKfsSharedNamespace(KFS_SHARED_NAMESPACE);
        awsSecretServiceImpl.setKfsInstanceNameSpace(KFS_INSTANCE_NAMESPACE);
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
        assertEquals(date.toString(), secretDate.toString());
    }
    
    @Test
    void testPojo() {
        String newUniqueString = UUID.randomUUID().toString();
        Date newDate = new Date(Calendar.getInstance().getTimeInMillis());
        
        AwsSecretePojoBasic pojo = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretePojoBasic.class);
        pojo.setChangeable_string(newUniqueString);
        pojo.setUpdate_date(newDate);;
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, false, pojo);
        
        AwsSecretePojoBasic pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, false, AwsSecretePojoBasic.class);
        assertEquals(newUniqueString, pojoNew.getChangeable_string());
        assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
        assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
        assertEquals(newDate.toString(), pojoNew.getUpdate_date().toString());
    }
    
    @Test 
    void testBuildFullAwsKeyNameAnyNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, false);
        String expectedFullNameSpace = KFS_SHARED_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }
    
    @Test 
    void testBuildFullAwsKeyNameInstanctNamespace() {
        String keyName = "foo";
        String actualFullNameSpace = awsSecretServiceImpl.buildFullAwsKeyName(keyName, true);
        String expectedFullNameSpace = KFS_INSTANCE_NAMESPACE + keyName;
        assertEquals(expectedFullNameSpace, actualFullNameSpace);
    }

}
