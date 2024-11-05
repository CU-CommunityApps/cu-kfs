package edu.cornell.kfs.concur.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.impl.ConcurAccountValidationServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class ConcurAccountValidationServiceTest {
    private static final Logger LOG = LogManager.getLogger();
    private ConcurAccountValidationServiceImpl concurAccountValidationService;
    private ConcurAccountInfo concurAccountInfo;
    
    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(ConcurAccountValidationServiceTest.class, Level.DEBUG);
        concurAccountValidationService = new ConcurAccountValidationServiceImpl();
        concurAccountValidationService.setAccountService(new MockAccountService());
        concurAccountValidationService.setObjectCodeService(new MockObjectCodeService());
        concurAccountValidationService.setSubObjectCodeService(new MockSubObjectCodeService());
        concurAccountValidationService.setProjectCodeService(new MockProjectCodeService());
        concurAccountValidationService.setSubAccountService(new MockSubAccountService());
        concurAccountValidationService.setConfigurationService(buildMockConfigurationService());
        concurAccountInfo = buildValidConcurAccountInfo();
    }
    
    private static ConfigurationService buildMockConfigurationService() {
        return new MockConfigurationService();
    }
    
    private ConcurAccountInfo buildValidConcurAccountInfo(){
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(
                ConcurAccountValidationTestConstants.VALID_CHART,
                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                ConcurAccountValidationTestConstants.VALID_PROJECT_CODE);
        return concurAccountInfo;
    }
    
    @AfterEach
    public void testDown() {    
        concurAccountInfo = null;
    }
    
    @ParameterizedTest
    @MethodSource("testCheckAccountParams")
    public void testCheckAccount(String chartCode, String accountNumber, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildAccountErrorMessage(chartCode, accountNumber, errorMessageKey));
        }
        ValidationResult validationResult = concurAccountValidationService.checkAccount(chartCode, accountNumber);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckAccount");
    }

    private static Stream<Arguments> testCheckAccountParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART, null, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR, false, KFSKeyConstants.ERROR_INACTIVE)
                );
    }
    
    private String buildAccountErrorMessage(String chart, String account, String errorMessageProperty) {
        String accountErrorMessageString = ConcurUtils
                .formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, chart, account);
        return MessageFormat.format(
                buildMockConfigurationService().getPropertyValueAsString(errorMessageProperty),
                accountErrorMessageString);
    }
    
    private void validateResults(boolean validationExpectation, List<String> expectedErrorMessages, ValidationResult validationResult, String functionName) {
        Assert.assertEquals("Validation was expected to be " + validationExpectation, validationExpectation, validationResult.isValid());
        Assert.assertEquals("Number of error messages is not what is exptected", expectedErrorMessages.size(), validationResult.getMessages().size());
        
        if (LOG.isDebugEnabled()) {
            for (String expectedMessage : expectedErrorMessages) {
                LOG.debug(functionName + ", expected error message: " + expectedMessage);
            }
            for (String actualMessage : validationResult.getMessages()) {
                LOG.debug(functionName + ", actual error message:   " + actualMessage);
            }
        }
        
        for (String expectedMessage : expectedErrorMessages) {
            boolean isExpectedMessageFound = validationResult.getMessages().contains(expectedMessage);
            Assert.assertTrue("expected to find " + expectedMessage, isExpectedMessageFound);
        }
    }
    
    @ParameterizedTest
    @MethodSource("testCheckObjectCodeParams")
    public void testCheckObjectCode(String chartCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildChartErrorMessage(chartCode, errorMessageKey));
        }
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, chartCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }
    
    private static Stream<Arguments> testCheckObjectCodeParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_OBJ_CD, true, null),
                Arguments.of(null, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.BAD_OBJ_CD, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD, false, KFSKeyConstants.ERROR_INACTIVE)
                );
    }
    
    private String buildChartErrorMessage(String chart, String errorMessageProperty) {
        String objectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(
                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE,
                ConcurAccountValidationTestConstants.VALID_CHART, chart);

        return MessageFormat.format(
                concurAccountValidationService.getConfigurationService().getPropertyValueAsString(errorMessageProperty),
                objectCodeErrorMessageString);
    }
    
    @ParameterizedTest
    @MethodSource("testCheckSubAccountParams")
    public void testCheckSubAccount(String chartCode, String accountNumber, String subAccountNumber, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildSubAccountErrorMessage(chartCode, accountNumber, subAccountNumber, errorMessageKey));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(chartCode, accountNumber, subAccountNumber);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }
    
    private static Stream<Arguments> testCheckSubAccountParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT, false, KFSKeyConstants.ERROR_INACTIVE)
                );
    }
    
    private String buildSubAccountErrorMessage(String chartCode, String accountNumber, String subAccountNumber, String errorMessageProperty) {
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, chartCode, accountNumber, subAccountNumber);

        return MessageFormat.format(
                concurAccountValidationService.getConfigurationService().getPropertyValueAsString(errorMessageProperty),
                subAccountErrorMessageString);
    }
    
    @ParameterizedTest
    @MethodSource("testcheckSubObjectCodeParams")
    public void testcheckSubObjectCode(String chartCode, String accountNumber, String objectCode, String subObjectCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildSubObjectCodeErrorMessage(chartCode, accountNumber, objectCode, subObjectCode, errorMessageKey));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(chartCode, accountNumber, objectCode, subObjectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testcheckSubObjectCode");
    }
    
    private static Stream<Arguments> testcheckSubObjectCodeParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT, false, KFSKeyConstants.ERROR_INACTIVE)
                );
    }
    
    private String buildSubObjectCodeErrorMessage(String chartCode, String accountNumber, String objectCode, String subObjectCode, String errorMessageProperty) {
        String subObjectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, chartCode, accountNumber, objectCode, subObjectCode);

        return MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(errorMessageProperty), subObjectCodeErrorMessageString);
    }
    
    @ParameterizedTest
    @MethodSource("testCheckProjectCodeParams")
    public void testCheckProjectCode(String projectCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildprojectCodeErrorMessage(projectCode, errorMessageKey));
        }
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(projectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testcheckSubObjectCode");
    }
    
    private static Stream<Arguments> testCheckProjectCodeParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, true, null),
                Arguments.of(null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE, false, KFSKeyConstants.ERROR_INACTIVE)
                );
    }
    
    private String buildprojectCodeErrorMessage(String projectCode, String errorMessageProperty) {
        String  projectCodeErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, projectCode);

        return MessageFormat.format(
                concurAccountValidationService.getConfigurationService().getPropertyValueAsString(errorMessageProperty),
                projectCodeErrorMessageString);
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
        String  accountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.BAD_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR);                        
        Assert.assertFalse("Validation was expected to fail but returned true", validationResult.isValid());
        Assert.assertEquals(
                "One error message was expected for bad chart",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), accountErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
    
    @Test
    public void isAccountingStringSubAccountBad(){
        concurAccountInfo.setSubAccountNumber(ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        String subAccountErrorMessageString = ConcurUtils.formatStringForErrorMessage(ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, ConcurAccountValidationTestConstants.VALID_CHART, ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.BAD_SUB_ACCT);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(concurAccountInfo);
        Assert.assertEquals(
                "One error message was expected for bad sub account",
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subAccountErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), accountErrorMessageString) + KFSConstants.NEWLINE,
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
                MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), objectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subAccountErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), subObjectCodeErrorMessageString) + KFSConstants.NEWLINE
                + MessageFormat.format(concurAccountValidationService.getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), projectCodeErrorMessageString) + KFSConstants.NEWLINE,
                validationResult.getErrorMessagesAsOneFormattedString());
    }
}
