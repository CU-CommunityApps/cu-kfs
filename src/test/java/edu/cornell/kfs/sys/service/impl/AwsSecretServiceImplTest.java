package edu.cornell.kfs.sys.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AwsSecretServiceImplTest {
    
    private static final String SINGLE_STRING_SECRET_KEY_NAME = "kfs/unittest/singlestring";
    private static final String SINGLE_STRING_SECRET_VALUE = "Test Value";
    private static final String SINGLE_DATE_SECRET_KEY_NAME = "kfs/unittest/singledate";
    
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
        System.out.println(dateString);
        
        Date returnDate = awsSecretServiceImpl.convertStringToDate(dateString);
        assertEquals(date.toString(), returnDate.toString());
        
    }

}
