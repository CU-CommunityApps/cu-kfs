package edu.cornell.kfs.sys.service.impl;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ErrorMessageUtilsServiceTest {
    
    private ErrorMessageUtilsServiceImpl errorMessageUtilsService;
    
    @Before
    public void setUp() throws Exception {
        errorMessageUtilsService = new ErrorMessageUtilsServiceImpl();
        errorMessageUtilsService.setConfigurationService(new MockConfigurationService());
    }
    
    @After
    public void testDown() {    
       
    } 
    
    @Test
    public void testCreateErrorSring(){
        String errorString = errorMessageUtilsService.createErrorString(ErrorMessageUtilsServiceTestConstants.ERROR_TEST_KEY, ErrorMessageUtilsServiceTestConstants.TEST_PARAM);
        Assert.assertEquals(ErrorMessageUtilsServiceTestConstants.ERROR_STRING, errorString);    
    }
    
    @Test
    public void testCreateErrorSringEmptyKey(){
        String errorString = errorMessageUtilsService.createErrorString(StringUtils.EMPTY, ErrorMessageUtilsServiceTestConstants.TEST_PARAM);
        Assert.assertEquals(ErrorMessageUtilsServiceTestConstants.ERROR_STRING_FOR_EMPTY_KEY, errorString);    
    }
}
