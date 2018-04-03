package edu.cornell.kfs.fp.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.service.impl.fixture.AmazonWebServiceBiillingServiceDateFixture;

public class AmazonWebServicesBillingServiceImplTest {

    private AmazonWebServicesBillingServiceImpl amazonService;

    @Before
    public void setUp() throws Exception {
        amazonService = new AmazonWebServicesBillingServiceImpl();
        amazonService.setParameterService(getMockedParameterService());
        amazonService.setConfigurationService(getMockedConfigurationService());
    }

    @After
    public void tearDown() throws Exception {
        amazonService = null;
    }
    
    private ParameterService getMockedParameterService() {
        ParameterService parameterService = mock(ParameterService.class);

        when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_PROCESSING_DATE_PROPERTY_NAME))
                .thenReturn(AmazonWebServiceBiillingServiceDateFixture.JULY_2016.processMonthInputParameter);


        return parameterService;
    }
    
    private ConfigurationService getMockedConfigurationService() {
        ConfigurationService configService = mock(ConfigurationService.class);
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_DESCRIPTION_FORMAT))
            .thenReturn("{0} invoice for {1}");
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_EXPLANATION_FORMAT))
            .thenReturn("AWS account {0}");
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ACCOUNTING_LINE_DESCRIPTION))
        .thenReturn("AWS CHARGES {0} {1}");
        
        return configService;
    }
    
    @Test
    public void testBuildDocumentExplanation() {
        amazonService.setBillingPeriodParameterValue(AmazonWebServiceBiillingServiceDateFixture.JUNE_2016.processMonthInputParameter);
        String AWSAccount = "12345";
        String results = amazonService.buildDocumentExplanation(AWSAccount);
        String expected = "AWS account " + AWSAccount;
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription1() {
        String departmentName = "CIT";
        String results = amazonService.buildDocumentDescription(departmentName);
        String expected = "July invoice for CIT";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription2() {
        String departmentName = "A REALLY LONG DEPARTMENT NAME";
        String results = amazonService.buildDocumentDescription(departmentName);
        String expected = "July invoice for A REALLY LONG DEPARTMEN";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildAccountingLineDescription() {
        amazonService.setBillingPeriodParameterValue(AmazonWebServiceBiillingServiceDateFixture.DECEMBER_2015.processMonthInputParameter);
        String results = amazonService.buildAccountingLineDescription();
        String expected = "AWS CHARGES December 2015";
        assertEquals(expected, results);
    }
    
    @Test
    public void testFindMonthInfoJuly() {
        validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture.JULY_2016);
        
    }
    
    @Test
    public void testFindMonthInfoJanuary() {
        validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture.JANUARY_2017);
        
    }
    
    @Test
    public void testFindMonthInfoDecember() {
        validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture.DECEMBER_2015);
        
    }
    
    @Test
    public void testFindMonthInfoLeapYear() {
        validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture.FEBRUARY_2016);
        
    }
    
    @Test
    public void testFindMonthInfoNonLeapFebruary() {
        validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture.FEBRUARY_2017);
        
    }
    
    protected void validateDateProcessing(AmazonWebServiceBiillingServiceDateFixture dateFixture) {
        amazonService.setBillingPeriodParameterValue(dateFixture.processMonthInputParameter);
        assertEquals(dateFixture.monthName, amazonService.findMonthName());
        assertEquals(dateFixture.monthNumber, amazonService.findProcessMonthNumber());
        assertEquals(dateFixture.year, amazonService.findProcessYear());
        assertEquals(dateFixture.startDate, amazonService.findStartDate());
        assertEquals(dateFixture.endDate, amazonService.findEndDate());
    }
    
    @Test
    public void testFindMonthInfoCurrent() {
        amazonService.setBillingPeriodParameterValue(CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        
        String expectedMonthName = new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)];
        String expectedMonthNumber = String.valueOf(cal.get(Calendar.MONTH) + 1);
        String expectedYear = String.valueOf(cal.get(Calendar.YEAR));
        assertEquals("Month Name isn't what we expected.", expectedMonthName, amazonService.findMonthName());
        assertEquals("Month isn't what we expected.", expectedMonthNumber, amazonService.findProcessMonthNumber());
        assertEquals("Year isn't what we expected.", expectedYear, amazonService.findProcessYear());
        
    }
    
    @Test
    public void testParseAWSAccountFromCloudCheckrGroupValue() {
        String cloudCheckrAWSAccount = "98643985626 (Cornell Departmental Account)";
        String expectedAWSAccount = "98643985626";
        String actualAWSAccount = amazonService.parseAWSAccountFromCloudCheckrGroupValue(cloudCheckrAWSAccount);
        assertEquals(expectedAWSAccount, actualAWSAccount);
    }
    
    @Test
    public void testParseDeaprtmentNameFromCloudCheckrGroupValue() {
        String cloudCheckrAWSAccount = "98643985626 (Cornell Departmental Account)";
        String expectedCornellDepartment = "Cornell Departmental Account";
        String actualCornellDepartment = amazonService.parseDeaprtmentNameFromCloudCheckrGroupValue(cloudCheckrAWSAccount);
        assertEquals(expectedCornellDepartment, actualCornellDepartment);
    }
}
