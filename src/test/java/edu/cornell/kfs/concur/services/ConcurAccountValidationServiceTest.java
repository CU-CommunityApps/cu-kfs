package edu.cornell.kfs.concur.services;

import java.text.MessageFormat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.impl.ConcurAccountValidationServiceImpl;

public class ConcurAccountValidationServiceTest {
    
    private ConcurAccountValidationServiceImpl concurAccountValidationService;
    private ConcurAccountInfo concurAccountInfo;
    
    @Before
    public void setUp() throws Exception {
        concurAccountValidationService = new ConcurAccountValidationServiceImpl();
        concurAccountValidationService.setAccountService(new MockAccountService());
        concurAccountValidationService.setObjectCodeService(new MockObjectCodeService());
        concurAccountValidationService.setSubObjectCodeService(new MockSubObjectCodeService());
        concurAccountValidationService.setProjectCodeService(new MockProjectCodeService());
        concurAccountValidationService.setSubAccountService(new MockSubAccountService());
        concurAccountValidationService.setConfigurationService(new MockConfigurationService());
        concurAccountInfo = buildValidConcurAccountInfo();
    }
    
    @After
    public void testDown() {    
        concurAccountInfo = null;
    }
    
    private ConcurAccountInfo buildValidConcurAccountInfo(){
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo();
        concurAccountInfo.setChart(ConcurAccountValidationTestConstants.VALID_CHART);
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.VALID_ACCT_NBR);
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.VALID_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.VALID_PROJECT_CODE);
        return concurAccountInfo;
    }
    
    @Test
    public void isAccountValid(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR);
        Assert.assertTrue("Validation was expected to pass", validationResult.isValid());
        Assert.assertEquals("No error messages expected", 0, validationResult.getMessages().size());    
    }
    
    @Test
    public void isAccountNull(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for null account number",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad account number",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_OBJ_CD);
        Assert.assertTrue("Validation was expected to pass", validationResult.isValid());
        Assert.assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isObjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for null object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountValid(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        Assert.assertTrue("Validation was expected to pass",validationResult.isValid());
        Assert.assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isSubAccountNull(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for null sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        Assert.assertTrue("Validation was expected to pass", validationResult.isValid());
        Assert.assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isSubObjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeObjectBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub object",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive sub object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE)+ KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.VALID_PROJECT_CODE);
        Assert.assertTrue("Validation was expected to pass", validationResult.isValid());
        Assert.assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }  
    
    @Test
    public void isProjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountInfoValid(){
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertTrue(validationResult.getErrorMessagesAsOneFormattedString(), validationResult.isValid());
    }
    
    @Test
    public void isChartMissing(){
        concurAccountInfo.setChart(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing chart",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.CHART) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountNumberMissing(){
        concurAccountInfo.setAccountNumber(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeMissing(){
        concurAccountInfo.setObjectCode(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isChartAccountAndObjectCodeMissing(){
        concurAccountInfo.setChart(null);
        concurAccountInfo.setAccountNumber(null);
        concurAccountInfo.setObjectCode(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.CHART) + KFSConstants.NEWLINE +
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE +
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringChartBad(){
        concurAccountInfo.setChart(ConcurAccountValidationTestConstants.BAD_CHART);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for bad chart",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringAccountBad(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for bad account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
   }
    
    @Test
    public void isAccountingStringAccountInactive(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for inactive account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountBad(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertEquals(
                "One error message was expected for bad sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountInactive(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for inactive sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeBad(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeInactive(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeBad(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad sub object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeInactive(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive sub object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeBad(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive object code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeInactive(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad inactive project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountObjectSubAccountSubObjectProjectBad() {
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad account, object, sub account, sub object and project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectSubAccountSubObjectProjectBad() {
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad  object, sub account, sub object and project code",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
}
