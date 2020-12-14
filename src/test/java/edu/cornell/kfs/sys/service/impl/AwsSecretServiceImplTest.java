package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.impl.fixture.AwsSecretePojoBasic;

class AwsSecretServiceImplTest {
    private static final Logger LOG = LogManager.getLogger(AwsSecretServiceImplTest.class);
    
    private static final String SINGLE_STRING_SECRET_KEY_NAME = "kfs/unittest/singlestring";
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "kfs/unittest/singledate";
    
    private static final String BASIC_POJO_SECRET_KEY_NAME = "kfs/unittest/pojo";
    private static final String BASIC_POJO_STATIC_STRING_VALUE = "do not change me";
    private static final int BASIC_POJO_NUMBER_VALUE = 1;
    
    
    private AwsSecretServiceImpl awsSecretServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        awsSecretServiceImpl = new AwsSecretServiceImpl();
        awsSecretServiceImpl.setAwsRegion("us-east-1");
    }

    @AfterEach
    void tearDown() throws Exception {
        awsSecretServiceImpl = null;
    }

    @Test
    void testGetSingleStringValueFromAwsSecret() {
        String actualSecretValue = awsSecretServiceImpl.getSingleStringValueFromAwsSecret(SINGLE_STRING_SECRET_KEY_NAME);
        assertEquals(SINGLE_STRING_SECRET_VALUE, actualSecretValue);
    }
    
    @Test
    void testDateConversionFunctions() throws ParseException {
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        String dateString = awsSecretServiceImpl.convertDateToString(date);
        LOG.info("testDateConversionFunctions, dateString: " + dateString);
        
        Date returnDate = awsSecretServiceImpl.convertStringToDate(dateString);
        assertEquals(date.toString(), returnDate.toString());
        
    }
    
    @Test
    void testDateSetAndGet() throws ParseException {
        Date date = new Date(Calendar.getInstance().getTimeInMillis());
        awsSecretServiceImpl.updateSecretDate(SINGLE_DATE_SECRET_KEY_NAME, date);
        
        Date secretDate = awsSecretServiceImpl.getSingleDateValueFromAwsSecret(SINGLE_DATE_SECRET_KEY_NAME);
        assertEquals(date.toString(), secretDate.toString());
    }
    
    @Test
    void testPojoBasic() {
        String newUniqueString = UUID.randomUUID().toString();;
        
        AwsSecretePojoBasic pojo = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, AwsSecretePojoBasic.class);
        pojo.setChangeable_string(newUniqueString);
        awsSecretServiceImpl.updatePojo(BASIC_POJO_SECRET_KEY_NAME, pojo);
        
        AwsSecretePojoBasic pojoNew = awsSecretServiceImpl.getPojoFromAwsSecret(BASIC_POJO_SECRET_KEY_NAME, AwsSecretePojoBasic.class);
        assertEquals(newUniqueString, pojoNew.getChangeable_string());
        assertEquals(BASIC_POJO_STATIC_STRING_VALUE, pojoNew.getStatic_string());
        assertEquals(BASIC_POJO_NUMBER_VALUE, pojoNew.getNumber_test());
        
    }

}
