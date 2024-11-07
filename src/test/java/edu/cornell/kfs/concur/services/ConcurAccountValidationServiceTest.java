package edu.cornell.kfs.concur.services;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

@Execution(SAME_THREAD)
public class ConcurAccountValidationServiceTest {
    private static final Logger LOG = LogManager.getLogger();
    private ConcurAccountValidationServiceImpl concurAccountValidationService;

    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(ConcurAccountValidationServiceTest.class, Level.DEBUG);
        concurAccountValidationService = new ConcurAccountValidationServiceImpl();
        concurAccountValidationService.setAccountService(new MockAccountService());
        concurAccountValidationService.setObjectCodeService(new MockObjectCodeService());
        concurAccountValidationService.setSubObjectCodeService(new MockSubObjectCodeService());
        concurAccountValidationService.setProjectCodeService(new MockProjectCodeService());
        concurAccountValidationService.setSubAccountService(new MockSubAccountService());
        concurAccountValidationService.setConfigurationService(new MockConfigurationService());
    }

    private static ConfigurationService getMockConfigurationService() {
        return new MockConfigurationService();
    }

    @AfterEach
    public void tearDown() {
        concurAccountValidationService = null;
    }

    @ParameterizedTest
    @MethodSource("testCheckAccountParams")
    public void testCheckAccount(String chartCode, String accountNumber, boolean validationExpectation,
            String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, chartCode, accountNumber));
        }
        ValidationResult validationResult = concurAccountValidationService.checkAccount(chartCode, accountNumber);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckAccount");
    }

    private static Stream<Arguments> testCheckAccountParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART, null, false,
                        KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR, false, KFSKeyConstants.ERROR_INACTIVE));
    }

    private static String buildFormattedMessage(String errorMessageProperty, String label, String... values) {
        String messageString = ConcurUtils.formatStringForErrorMessage(label, values);
        return MessageFormat.format(getMockConfigurationService().getPropertyValueAsString(errorMessageProperty),
                messageString);
    }

    private static String buildFormattedMessage(String errorMessageProperty, String label) {
        return MessageFormat.format(getMockConfigurationService().getPropertyValueAsString(errorMessageProperty),
                label);
    }

    private void validateResults(boolean validationExpectation, List<String> expectedErrorMessages,
            ValidationResult validationResult, String functionName) {
        if (LOG.isDebugEnabled()) {
            for (String expectedMessage : expectedErrorMessages) {
                LOG.debug(functionName + ", expected error message: " + expectedMessage);
            }
            for (String actualMessage : validationResult.getMessages()) {
                LOG.debug(functionName + ", actual error message:   " + actualMessage);
            }
        }

        Assert.assertEquals("Validation was expected to be " + validationExpectation, validationExpectation,
                validationResult.isValid());
        Assert.assertEquals("Number of error messages is not what is exptected", expectedErrorMessages.size(),
                validationResult.getMessages().size());
        for (String expectedMessage : expectedErrorMessages) {
            boolean isExpectedMessageFound = validationResult.getMessages().contains(expectedMessage);
            Assert.assertTrue("expected to find " + expectedMessage, isExpectedMessageFound);
        }
        
        String expectedFormatedString = StringUtils.EMPTY;
        for (String expectedMessage : expectedErrorMessages) {
            expectedFormatedString = expectedFormatedString + expectedMessage + KFSConstants.NEWLINE;
        }
        Assert.assertEquals(expectedFormatedString, validationResult.getErrorMessagesAsOneFormattedString());
    }

    @ParameterizedTest
    @MethodSource("testCheckObjectCodeParams")
    public void testCheckObjectCode(String chartCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages
                    .add(buildFormattedMessage(errorMessageKey, ConcurConstants.AccountingStringFieldNames.OBJECT_CODE,
                            ConcurAccountValidationTestConstants.VALID_CHART, chartCode));
        }
        ValidationResult validationResult = concurAccountValidationService
                .checkObjectCode(ConcurAccountValidationTestConstants.VALID_CHART, chartCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }

    private static Stream<Arguments> testCheckObjectCodeParams() {
        return Stream.of(Arguments.of(ConcurAccountValidationTestConstants.VALID_OBJ_CD, true, null),
                Arguments.of(null, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.BAD_OBJ_CD, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD, false,
                        KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testCheckSubAccountParams")
    public void testCheckSubAccount(String chartCode, String accountNumber, String subAccountNumber,
            boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, chartCode, accountNumber,
                    subAccountNumber));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(chartCode, accountNumber,
                subAccountNumber);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }

    private static Stream<Arguments> testCheckSubAccountParams() {
        return Stream.of(
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT, false, KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testcheckSubObjectCodeParams")
    public void testcheckSubObjectCode(String chartCode, String accountNumber, String objectCode, String subObjectCode,
            boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(
                    buildFormattedMessage(errorMessageKey, ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                            chartCode, accountNumber, objectCode, subObjectCode));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(chartCode, accountNumber,
                objectCode, subObjectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testcheckSubObjectCode");
    }

    private static Stream<Arguments> testcheckSubObjectCodeParams() {
        return Stream.of(Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                ConcurAccountValidationTestConstants.VALID_ACCT_NBR, ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                ConcurAccountValidationTestConstants.VALID_SUB_OBJECT, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD, null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT, false,
                        KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testCheckProjectCodeParams")
    public void testCheckProjectCode(String projectCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, projectCode));
        }
        ValidationResult validationResult = concurAccountValidationService.checkProjectCode(projectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testcheckSubObjectCode");
    }

    private static Stream<Arguments> testCheckProjectCodeParams() {
        return Stream.of(Arguments.of(ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, true, null),
                Arguments.of(null, true, null),
                Arguments.of(ConcurAccountValidationTestConstants.BAD_PROJECT_CODE, false,
                        KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE, false,
                        KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testValidateConcurAccountInfoParams")
    public void testValidateConcurAccountInfo(String chart, String account, String subAccount, String object,
            String subObject, String project, boolean validationExpectation, List<String> expectedErrorMessages) {
        ConcurAccountInfo accountInfo = new ConcurAccountInfo(chart, account, subAccount, object, subObject, project);
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(accountInfo);
        validateResults(validationExpectation, expectedErrorMessages, validationResult,
                "testValidateConcurAccountInfo");
    }

    private static Stream<Arguments> testValidateConcurAccountInfoParams() {
        return Stream.of(Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                ConcurAccountValidationTestConstants.VALID_SUB_ACCT, ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, true, buildMessages()),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, StringUtils.EMPTY,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD, StringUtils.EMPTY, StringUtils.EMPTY, true,
                        buildMessages()),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR, null,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD, null, null, true, buildMessages()),
                Arguments.of(null, ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.CHART))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART, null,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT, null,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
                Arguments.of(null, null, ConcurAccountValidationTestConstants.VALID_SUB_ACCT, null,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(
                                buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                        ConcurConstants.AccountingStringFieldNames.CHART),
                                buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER),
                                buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
                Arguments.of(null, ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT, null,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(
                                buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                        ConcurConstants.AccountingStringFieldNames.CHART),
                                buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
                Arguments.of(ConcurAccountValidationTestConstants.BAD_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.BAD_CHART,
                                ConcurAccountValidationTestConstants.VALID_ACCT_NBR))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.BAD_ACCT_NBR))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.INACTIVE_ACCT_NBR))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                ConcurAccountValidationTestConstants.BAD_SUB_ACCT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                ConcurAccountValidationTestConstants.INACTIVE_SUB_ACCT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.BAD_OBJ_CD),
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(
                                buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD),
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                        ConcurAccountValidationTestConstants.INACTIVE_OBJ_CD,
                                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                                ConcurAccountValidationTestConstants.BAD_SUB_OBJECT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.VALID_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                                ConcurAccountValidationTestConstants.INACTIVE_SUB_OBJECT))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.BAD_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.PROJECT_CODE,
                                ConcurAccountValidationTestConstants.BAD_PROJECT_CODE))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.VALID_SUB_ACCT,
                        ConcurAccountValidationTestConstants.VALID_OBJ_CD,
                        ConcurAccountValidationTestConstants.VALID_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                ConcurConstants.AccountingStringFieldNames.PROJECT_CODE,
                                ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.BAD_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT,
                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.BAD_PROJECT_CODE, false,
                        buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER,
                                ConcurAccountValidationTestConstants.VALID_CHART,
                                ConcurAccountValidationTestConstants.BAD_ACCT_NBR))),
                Arguments.of(ConcurAccountValidationTestConstants.VALID_CHART,
                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT,
                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT,
                        ConcurAccountValidationTestConstants.BAD_PROJECT_CODE, false,
                        buildMessages(
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.BAD_OBJ_CD),
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                        ConcurAccountValidationTestConstants.BAD_SUB_ACCT),
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE,
                                        ConcurAccountValidationTestConstants.VALID_CHART,
                                        ConcurAccountValidationTestConstants.VALID_ACCT_NBR,
                                        ConcurAccountValidationTestConstants.BAD_OBJ_CD,
                                        ConcurAccountValidationTestConstants.BAD_SUB_OBJECT),
                                buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                        ConcurConstants.AccountingStringFieldNames.PROJECT_CODE,
                                        ConcurAccountValidationTestConstants.BAD_PROJECT_CODE))));
    }

    private static List<String> buildMessages(String... messages) {
        List<String> messageList = new ArrayList<>();
        for (String message : messages) {
            messageList.add(message);
        }
        return messageList;
    }
}
