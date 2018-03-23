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

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.service.impl.fixture.AmazonWebServiceBiillingServiceDateFixture;

public class AmazonWebServicesBillingServiceImplTest {

    public static final String APRIL_2016_PARAM_VALUE = "2106,4";
    public static final String JUNE_2016_PARAM_VALUE = "2016,6";
    public static final String DEC_2015_PARAM_VALUE = "2015,12";
    private static double allowableVarianceAmount = 0;
    
    private AmazonWebServicesBillingServiceImpl amazonService;

    @Before
    public void setUp() throws Exception {
        amazonService = new AmazonWebServicesBillingServiceImpl();
        amazonService.setParameterService(getMockedParameterService());
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
    
    @Test
    public void testBuildDocumentExplanation() {
        amazonService.setBillingPeriodParameterValue(JUNE_2016_PARAM_VALUE);
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
        amazonService.setBillingPeriodParameterValue(DEC_2015_PARAM_VALUE);
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
