package edu.cornell.kfs.concur.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.impl.ConcurAccountValidationServiceImpl;
import static org.junit.Assert.assertEquals;

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
        assertEquals("Validation was expected to pass", true, validationResult.isValid());
        assertEquals("No error messages expected", 0, validationResult.getMessages().size());    
    }
    
    @Test
    public void isAccountNull(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, null);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for null account number",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad account number",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for inactive account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_OBJ_CD);
        assertEquals("Validation was expected to pass", true, validationResult.isValid());
        assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isObjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, null);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for null object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for inactive object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountValid(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        assertEquals("Validation was expected to pass", true, validationResult.isValid());
        assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isSubAccountNull(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, null);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for null sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST+ KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST+ KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountAccountBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.BAD_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_SUB_ACCT);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubAccountInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for inactive sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        assertEquals("Validation was expected to pass", true, validationResult.isValid());
        assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }
    
    @Test
    public void isSubObjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad sub object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, null);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad sub object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeObjectBad(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_OBJ_CD, ConcurAccountValidationTestConstants.VALID_SUB_OBJECT);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad sub object",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isSubObjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD, ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
          assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for inactive sub object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_INACTIVE+ KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeValid(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.VALID_PROJECT_CODE);
        assertEquals("Validation was expected to pass", true, validationResult.isValid());
        assertEquals("No error messages expected", 0, validationResult.getMessages().size());
    }  
    
    @Test
    public void isProjectCodeNull(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(null);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeBad(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for bad project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isProjectCodeInactive(){
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error message was expected for inactive project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountInfoValid(){
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals(validationResult.getErrorMessagesAsOneFormattedString(), true, validationResult.isValid());
    }
    
    @Test
    public void isChartMissing(){
        concurAccountInfo.setChart(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for missing chart",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_CHART_OF_ACCTS_REQUIRED + "\n",
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountNumberMissing(){
        concurAccountInfo.setAccountNumber(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for missing account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_NBR_REQUIRED + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectCodeMissing(){
        concurAccountInfo.setObjectCode(null);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for missing object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_REQUIRED + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringChartBad(){
        concurAccountInfo.setChart(ConcurAccountValidationTestConstants.BAD_CHART);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for bad chart",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringAccountBad(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.BAD_ACCT_NBR);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for bad account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
   }
    
    @Test
    public void isAccountingStringAccountInactive(){
        concurAccountInfo.setAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for inactive account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountBad(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for bad sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountInactive(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "One error message was expected for inactive sub account",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeBad(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for bad object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE
                + ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringObjectCodeInactive(){
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for inactive object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_INACTIVE + KFSConstants.NEWLINE
                + ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeBad(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for bad sub object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubObjectCodeInactive(){
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for inactive sub object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_INACTIVE + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeBad(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for inactive object code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringProjectCodeInactive(){
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for bad inactive project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_INACTIVE + KFSConstants.NEWLINE,
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
        assertEquals("Validation was expected to fail but returned true", false, validationResult.isValid());
        assertEquals(
                "Error messages expected for bad account, object, sub account, sub object and project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isObjectSubAccountSubObjectProjectBad() {
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        concurAccountInfo.setObjectCode(ConcurAccountValidationTestConstants.BAD_OBJ_CD);
        concurAccountInfo.setSubObjectCode(ConcurAccountValidationTestConstants.BAD_SUB_OBJECT);
        concurAccountInfo.setProjectCode(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        assertEquals("Validation was expected to fail but returned true",false, validationResult.isValid());
        assertEquals(
                "Error messages expected for bad  object, sub account, sub object and project code",
                ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE
                + ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST + KFSConstants.NEWLINE
                + ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE
                + ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_DOES_NOT_EXIST + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
}
