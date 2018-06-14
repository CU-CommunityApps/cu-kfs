package edu.cornell.kfs.fp.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.service.impl.fixture.AmazonWebServiceBillingServiceDateFixture;

public class AmazonWebServicesBillingServiceImplTest {

    private AmazonWebServicesBillingServiceImpl amazonService;

    @Before
    public void setUp() throws Exception {
        amazonService = new AmazonWebServicesBillingServiceImpl();
        amazonService.setParameterService(getMockedParameterService(AmazonWebServiceBillingServiceDateFixture.JULY_2016.processMonthInputParameter));
        amazonService.setConfigurationService(getMockedConfigurationService());
        amazonService.setDataDictionaryService(getMockedDataDictionaryService());
    }

    @After
    public void tearDown() throws Exception {
        amazonService = null;
    }
    
    private ParameterService getMockedParameterService(String processMonth) {
        ParameterService parameterService = mock(ParameterService.class);

        when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_PROCESSING_DATE_PARAMETER_NAME))
        .thenReturn(processMonth);
        
        when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CORNELL_MASTER_ACCOUNTS_PARAMETER_NAME))
        .thenReturn("078742956215=Cornell Master;951690301649=Cornell Master v2;361772241175=Cornell Master Sandbox v2");

        return parameterService;
    }
    
    private ConfigurationService getMockedConfigurationService() {
        ConfigurationService configService = mock(ConfigurationService.class);
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_DESCRIPTION_FORMAT))
            .thenReturn("AWS {0}-{1} invoice for {2}");
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_DOCUMENT_EXPLANATION_FORMAT))
            .thenReturn("AWS account {0}");
        
        when(configService.getPropertyValueAsString(CuFPKeyConstants.AWS_BILLING_SERVICE_ACCOUNTING_LINE_DESCRIPTION))
        .thenReturn("AWS CHARGES {0} {1}");
        
        return configService;
    }
    
    private DataDictionaryService getMockedDataDictionaryService() {
        DataDictionaryService ddService = mock(DataDictionaryService.class);
        when(ddService.getAttributeMaxLength(DocumentHeader.class, KFSPropertyConstants.DOCUMENT_DESCRIPTION)).thenReturn(40);
        return ddService;
    }
    
    @Test
    public void testBuildDocumentExplanation() {
        amazonService.setParameterService(getMockedParameterService(AmazonWebServiceBillingServiceDateFixture.JUNE_2016.processMonthInputParameter));
        String AWSAccount = "12345";
        String results = amazonService.buildDocumentExplanation(AWSAccount);
        String expected = "AWS account " + AWSAccount;
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription1() {
        String departmentName = "CIT";
        String results = amazonService.buildDocumentDescription(departmentName);
        String expected = "AWS July-2016 invoice for CIT";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription2() {
        String departmentName = "A REALLY LONG DEPARTMENT NAME";
        String results = amazonService.buildDocumentDescription(departmentName);
        String expected = "AWS July-2016 invoice for A REALLY LONG ";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildAccountingLineDescription() {
        amazonService.setParameterService(getMockedParameterService(AmazonWebServiceBillingServiceDateFixture.DECEMBER_2015.processMonthInputParameter));
        String results = amazonService.buildAccountingLineDescription();
        String expected = "AWS CHARGES December 2015";
        assertEquals(expected, results);
    }
    
    @Test
    public void testFindMonthInfoJuly() {
        validateDateProcessing(AmazonWebServiceBillingServiceDateFixture.JULY_2016);
        
    }
    
    @Test
    public void testFindMonthInfoJanuary() {
        validateDateProcessing(AmazonWebServiceBillingServiceDateFixture.JANUARY_2017);
        
    }
    
    @Test
    public void testFindMonthInfoDecember() {
        validateDateProcessing(AmazonWebServiceBillingServiceDateFixture.DECEMBER_2015);
        
    }
    
    @Test
    public void testFindMonthInfoLeapYear() {
        validateDateProcessing(AmazonWebServiceBillingServiceDateFixture.FEBRUARY_2016);
        
    }
    
    @Test
    public void testFindMonthInfoNonLeapFebruary() {
        validateDateProcessing(AmazonWebServiceBillingServiceDateFixture.FEBRUARY_2017);
        
    }
    
    protected void validateDateProcessing(AmazonWebServiceBillingServiceDateFixture dateFixture) {
        amazonService.setParameterService(getMockedParameterService(dateFixture.processMonthInputParameter));
        assertEquals(dateFixture.monthName, amazonService.findMonthName());
        assertEquals(dateFixture.monthNumber, amazonService.findProcessMonthNumber());
        assertEquals(dateFixture.year, amazonService.findProcessYear());
        assertEquals(dateFixture.startDate, amazonService.findStartDate());
        assertEquals(dateFixture.endDate, amazonService.findEndDate());
    }
    
    @Test
    public void testFindMonthInfoCurrent() {
        amazonService.setParameterService(getMockedParameterService(CuFPConstants.AmazonWebServiceBillingConstants.DEFAULT_BILLING_PERIOD_PARAMETER));
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
    public void testParseDepartmentNameFromCloudCheckrGroupValue() {
        String cloudCheckrAWSAccount = "98643985626 (Cornell Departmental Account)";
        String expectedCornellDepartment = "Cornell Departmental Account";
        String actualCornellDepartment = amazonService.parseDepartmentNameFromCloudCheckrGroupValue(cloudCheckrAWSAccount);
        assertEquals(expectedCornellDepartment, actualCornellDepartment);
    }
    
    @Test
    public void testBuildMasterAccountMap() {
        Map<String, String> masterAccountMap = amazonService.buildMasterAccountMap();
        
        assertEquals("Cornell Master", masterAccountMap.get("078742956215"));
        assertEquals("Cornell Master v2", masterAccountMap.get("951690301649"));
        assertEquals("Cornell Master Sandbox v2", masterAccountMap.get("361772241175"));
        assertEquals(3, masterAccountMap.keySet().size());
    }
}
