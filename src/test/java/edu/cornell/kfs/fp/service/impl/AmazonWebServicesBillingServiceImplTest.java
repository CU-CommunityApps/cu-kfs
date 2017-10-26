package edu.cornell.kfs.fp.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.AmazonBillingCostCenterDTO;
import edu.cornell.kfs.fp.xmlObjects.AmazonAccountDetail;

public class AmazonWebServicesBillingServiceImplTest {

    public static final String JSON_EXAMPLE_1_ENTRY = "{ \"account_detail\": [ { \"aws_account\": \"036869565879\", \"kfs_account\": \"G123008\", \"cost_center\": \"\", \"business_purpose\": \"this is a test\", \"cost\": \"605.5106149888798\" }]}";
    public static final String JSON_EXAMPLE = "{ \"account_detail\": [" 
            + "{ \"aws_account\": \"noCostCenter\", \"kfs_account\": \"G123008\", \"cost_center\": \"\", \"business_purpose\": \"this is a test\", \"cost\": \"605.5106149888798\" },"
            + "{ \"aws_account\": \"acctSubAcct1\", \"kfs_account\": \"F568822\", \"cost_center\": \"L023105-LSAWS\", \"business_purpose\": \"\",\"cost\": \"774.2422424733849\"},"
            + "{ \"aws_account\": \"acctSubacct2\", \"kfs_account\": \"F568822\", \"cost_center\": \"L023105-LSAWX\", \"business_purpose\": \"\",\"cost\": \"774.2422424733849\"},"
            + "{ \"aws_account\": \"FullStringInvProj\", \"kfs_account\": \"F568822\", \"cost_center\": \"IT-L023105-LSAWS-6580-SFT-AWS-test\", \"business_purpose\": \"\",\"cost\": \"774.2422424733849\"},"
            + "{ \"aws_account\": \"chartAcctObjOrg\", \"kfs_account\": \"R513810\", \"cost_center\": \"IT-R513810- -6601- - -TSTOrgRef\", \"business_purpose\": \"\",\"cost\": \"774.2422424733849\"},"
            + "{ \"aws_account\": \"chartAcctObj\", \"kfs_account\": \"R513810\", \"cost_center\": \"IT-R513810- -6603- - - \", \"business_purpose\": \"\",\"cost\": \"774.2422424733849\"},"
            + "{ \"aws_account\": \"078742956215\",\"kfs_account\": \"internal\",\"cost_center\": \"\",\"business_purpose\": \"this is a test\", \"cost\": \"5673.399876482097\"}" 
            + "]}";
    public static final String APRIL_2016_PARAM_VALUE = "2106,4";
    public static final String JUNE_2016_PARAM_VALUE = "2016,6";
    public static final String DEC_2015_PARAM_VALUE = "2015,12";
    private static double allowableVarianceAmount = 0;
    
    private AmazonWebServicesBillingServiceImpl amazonService;

    @Before
    public void setUp() throws Exception {
        amazonService = new AmazonWebServicesBillingServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        amazonService = null;
    }

    @Test
    public void testBuildAmazonAcountListFromJson1() {
        List<AmazonAccountDetail> details = amazonService.buildAmazonAcountListFromJson(JSON_EXAMPLE);
        assertEquals(7, details.size());
    }
    
    @Test
    public void testBuildAmazonAcountListFromJson2() {
        List<AmazonAccountDetail> details = amazonService.buildAmazonAcountListFromJson(JSON_EXAMPLE_1_ENTRY);
        assertEquals(1, details.size());
    }
    
    @Test
    public void testBuildAmazonAcountListFromJson3() {
        try {
            List<AmazonAccountDetail> details = amazonService.buildAmazonAcountListFromJson("");
        } catch (RuntimeException e) {
            if (StringUtils.contains(e.getMessage(), "com.fasterxml.jackson.databind.exc.MismatchedInputException")){
                assertTrue("Expected a Mismatched Input error", true);
                return;
            }
        }
        assertTrue("Expected a Mismatched Input error", false);
    }
    
    @Test
    public void testConvertCostStringToKualiDecimal1() {
        KualiDecimal results =amazonService.convertCostStringToKualiDecimal("100");
        double expected = 100;
        assertEquals(expected, results.doubleValue(), allowableVarianceAmount);
    }
    @Test
    public void testConvertCostStringToKualiDecimal2() {
        KualiDecimal results =amazonService.convertCostStringToKualiDecimal(".01");
        double expected = .01;
        assertEquals(expected, results.doubleValue(), allowableVarianceAmount);
    }
    
    
    @Test
    public void testConvertCostStringToKualiDecimal3() {
        KualiDecimal results =amazonService.convertCostStringToKualiDecimal(".0002");
        double expected = 0;
        assertEquals(expected, results.doubleValue(), allowableVarianceAmount);
    }
    
    @Test
    public void testConvertCostStringToKualiDecimal4() {
        KualiDecimal results =amazonService.convertCostStringToKualiDecimal("-50");
        double expected = 0;
        assertEquals(expected, results.doubleValue(), allowableVarianceAmount);
    }
    
    @Test
    public void testConvertCostStringToKualiDecimal5() {
        KualiDecimal results =amazonService.convertCostStringToKualiDecimal("");
        double expected = 0;
        assertEquals(expected, results.doubleValue(), allowableVarianceAmount);
    }
    
    @Test
    public void testConvertCostStringToKualiDecimal6() {
        boolean caughtNFE = false;
        try {
            KualiDecimal results =amazonService.convertCostStringToKualiDecimal("abc");
        } catch (NumberFormatException nfe) {
            caughtNFE = true;
        }
        assertTrue("We should get a NumberFormatExecption", caughtNFE);
    }
    
    @Test
    public void testBuildDocumentExplanation() {
        amazonService.setBillingPeriodParameterValue(JUNE_2016_PARAM_VALUE);
        String AWSAccount = "12345";
        String results = amazonService.buildDocumentExplanation(AWSAccount);
        String expected = "AWS charges for account number " + AWSAccount + " for June 2016";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription1() {
        String defaultDocumentDescription = "default description is really really really long";
        amazonService.setDefaultDocumentDescription(defaultDocumentDescription);
        String businessPurpose = "cool business purpose that goes on for a  while";
        String results = amazonService.buildDocumentDescription(businessPurpose);
        String expected = StringUtils.substring(businessPurpose, 0, 40);
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription2() {
        String defaultDocumentDescription = "default description is really really really long";
        amazonService.setDefaultDocumentDescription(defaultDocumentDescription);
        String businessPurpose = "";
        String results = amazonService.buildDocumentDescription(businessPurpose);
        String expected = StringUtils.substring(defaultDocumentDescription, 0, 40);
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildDocumentDescription3() {
        String defaultDocumentDescription = "default description";
        amazonService.setDefaultDocumentDescription(defaultDocumentDescription);
        String businessPurpose = "";
        String results = amazonService.buildDocumentDescription(businessPurpose);
        assertEquals(defaultDocumentDescription, results);
    }
    
    @Test
    public void testBuildAccountingLineDescription() {
        amazonService.setBillingPeriodParameterValue(DEC_2015_PARAM_VALUE);
        String results = amazonService.buildAccountingLineDescription();
        String expected = "AWS CHARGES December 2015";
        assertEquals(expected, results);
    }
    
    @Test
    public void testBuildAwsUrlForClientRequest() {
        String awsURL = "http://www.foo.bar/service?";
        String awsToken = "someDummyText";
        
        amazonService.setAwsURL(awsURL);
        amazonService.setAwsToken(awsToken);
        amazonService.setBillingPeriodParameterValue(DEC_2015_PARAM_VALUE);
        
        URI awsServiceUrl = amazonService.buildAwsServiceUrl();
        String resultsURL = awsServiceUrl.toString();
        String expectedURL = awsURL + "year=2015&month=12";
        assertEquals(expectedURL, resultsURL);
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOBlankAccount() throws Exception {
        String costCenter = "";
        String expected = "------";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Empty Account input not handled correctly", expected, dto.toString());
        AmazonBillingCostCenterDTO dtoNullAcct = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(null);
        assertEquals("Null Account input not handled correctly", expected, dtoNullAcct.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOAccount() throws Exception {
        String costCenter = "G234715";
        String expected = "-G234715-----";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Account input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOAccountSubAccount() throws Exception {
        String costCenter = "G234715-1234";
        String expected = "-G234715-1234----";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Account - Sub Account input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOFullTrans() throws Exception {
        String costCenter = "chart-acct-subAcct-Object-SubObject-Project-OrgRef";
        String expected = "chart-acct-subAcct-Object-SubObject-Project-OrgRef";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Full tansaction input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOFullTransNoSubAcct() throws Exception {
        String costCenter = "chart-acct--Object-SubObject-Project-OrgRef";
        String expected = "chart-acct--Object-SubObject-Project-OrgRef";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Full tansaction input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOFullTransJustAcctObj() throws Exception {
        String costCenter = "-acct--Object---";
        String expected = "-acct--Object---";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Full tansaction with just account and object input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testConvertCostCenterToAmazonBillingCostCenterDTOFullTransJustAcctObjOrgRef() throws Exception {
        String costCenter = "-acct--Object---orgRef";
        String expected = "-acct--Object---orgRef";
        AmazonBillingCostCenterDTO dto = amazonService.convertCostCenterToAmazonBillingCostCenterDTO(costCenter);
        assertEquals("Full tansaction with just account, object and orgRef input not handled correctly", expected, dto.toString());
    }
    
    @Test
    public void testFindMonthInfoJuly() {
        amazonService.setBillingPeriodParameterValue("2016,7");
        String expectedMonthName = "July";
        String expectedMonthNumber = "7";
        String expectedYear = "2016";
        assertEquals("Expected the month to be July, but was not.", expectedMonthName, amazonService.findMonthName());
        assertEquals("Expected the month to be 7, but was not.", expectedMonthNumber, amazonService.findProcessMonthNumber());
        assertEquals("Expected the year to be 2016, but was not.", expectedYear, amazonService.findProcessYear());
        
    }
    
    @Test
    public void testFindMonthInfoJanuary() {
        amazonService.setBillingPeriodParameterValue("2017,1");
        String expectedMonthName = "January";
        String expectedMonthNumber = "1";
        String expectedYear = "2017";
        assertEquals("Expected the month to be January, but was not.", expectedMonthName, amazonService.findMonthName());
        assertEquals("Expected the month to be 1, but was not.", expectedMonthNumber, amazonService.findProcessMonthNumber());
        assertEquals("Expected the year to be 2017, but was not.", expectedYear, amazonService.findProcessYear());
        
    }
    
    @Test
    public void testFindMonthInfoDecember() {
        amazonService.setBillingPeriodParameterValue("2015,12");
        String expectedMonthName = "December";
        String expectedMonthNumber = "12";
        String expectedYear = "2015";
        assertEquals("Expected the month to be December, but was not.", expectedMonthName, amazonService.findMonthName());
        assertEquals("Expected the month to be 12, but was not.", expectedMonthNumber, amazonService.findProcessMonthNumber());
        assertEquals("Expected the year to be 2015, but was not.", expectedYear, amazonService.findProcessYear());
        
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
}
