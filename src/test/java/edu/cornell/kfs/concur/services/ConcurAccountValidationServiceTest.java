package edu.cornell.kfs.concur.services;

import java.text.MessageFormat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.impl.ConcurAccountValidationServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;

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
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(
                ConcurAccountValidationTestConstants.VALID_CHART,
                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, null);
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
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for null account number",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);        
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad account number",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), accountErrorMessageString) + KFSConstants.NEWLINE,
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
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, null);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for null object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), objectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
        Assert.assertTrue("Validation was expected to pass", validationResult.isValid());
    }
    
    @Test
    public void isSubAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);      
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive sub account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
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
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, null);
        Assert.assertTrue("Validation was expected pass", validationResult.isValid());
    }
    
    @Test
    public void isSubObjectCodeObjectBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad sub object",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive sub object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subObjectCodeErrorMessageString)+ KFSConstants.NEWLINE,
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
        Assert.assertTrue("Validation was expected to succeed", validationResult.isValid());
    }
    
    @Test
    public void isProjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for bad project code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error message was expected for inactive project code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.CHART) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountNumberMissing(){
        concurAccountInfo.setAccountNumber(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeMissing(){
        concurAccountInfo.setObjectCode(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for missing object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
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
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.CHART) + KFSConstants.NEWLINE +
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER) + KFSConstants.NEWLINE +
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringChartBad(){
        concurAccountInfo.setChart(ConcurAccountValidationTestConstants.BAD_CHART);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.BAD_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR);                        
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for bad chart",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringAccountBad(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);        
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for bad account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
   }
    
    @Test
    public void isAccountingStringAccountInactive(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);        
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for inactive account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountBad(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertEquals(
                "One error message was expected for bad sub account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountInactive(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        String subAccountErrorMessageString = ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER + CUKFSConstants.COLON + ConcurAccountValidationTestConstants.VALID_CHART + KFSConstants.COMMA + ConcurAccountValidationTestConstants.VALID_ACCT_NBR + KFSConstants.COMMA + ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT;
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for inactive sub account",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeBad(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART,  ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeInactive(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeBad(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad sub object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeInactive(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive sub object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeBad(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for inactive object code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeInactive(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad inactive project code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountObjectSubAccountSubObjectProjectBad() {
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);        
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad account, object, sub account, sub object and project code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectSubAccountSubObjectProjectBad() {
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "Error messages expected for bad  object, sub account, sub object and project code",
                ConcurConstants.ERROR_MESSAGE_HEADER + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
}
