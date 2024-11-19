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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.impl.ConcurAccountValidationServiceImpl;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.AccountEnum;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.ConcurAccountInfoEnum;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.ObjectCodeEnum;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.ProjectCodeEnum;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.SubAccountEnum;
import edu.cornell.kfs.concur.services.ConcurAccountValidationTestConstants.SubObjectCodeEnum;

@Execution(SAME_THREAD)
public class ConcurAccountValidationServiceTest {
    private static final Logger LOG = LogManager.getLogger();
    private ConcurAccountValidationServiceImpl concurAccountValidationService;

    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(ConcurAccountValidationServiceTest.class, Level.DEBUG);
        concurAccountValidationService = new ConcurAccountValidationServiceImpl();
        concurAccountValidationService.setAccountService(buildMockAccountService());
        concurAccountValidationService.setObjectCodeService(buildMockObjectCodeService());
        concurAccountValidationService.setSubObjectCodeService(buildMockSubObjectCodeService());
        concurAccountValidationService.setProjectCodeService(buildMockProjectCodeService());
        concurAccountValidationService.setSubAccountService(buildMockSubAccountService());
        concurAccountValidationService
                .setConfigurationService(ConcurAccountValidationTestConstants.buildMockConfigurationService());
    }

    private AccountService buildMockAccountService() {
        AccountService service = Mockito.mock(AccountService.class);
        Mockito.when(service.getByPrimaryId(AccountEnum.VALID.chart, AccountEnum.VALID.account))
                .thenReturn(AccountEnum.VALID.toAccountBo());
        Mockito.when(service.getByPrimaryId(AccountEnum.INACTIVE.chart, AccountEnum.INACTIVE.account))
                .thenReturn(AccountEnum.INACTIVE.toAccountBo());
        Mockito.when(service.getByPrimaryId(AccountEnum.CLOSED.chart, AccountEnum.CLOSED.account))
                .thenReturn(AccountEnum.CLOSED.toAccountBo());
        return service;
    }

    private ObjectCodeService buildMockObjectCodeService() {
        ObjectCodeService service = Mockito.mock(ObjectCodeService.class);
        Mockito.when(service.getByPrimaryIdForCurrentYear(ObjectCodeEnum.VALID.chart, ObjectCodeEnum.VALID.objectCode))
                .thenReturn(ObjectCodeEnum.VALID.toObjectCode());
        Mockito.when(service.getByPrimaryIdForCurrentYear(ObjectCodeEnum.INACTIVE.chart,
                ObjectCodeEnum.INACTIVE.objectCode)).thenReturn(ObjectCodeEnum.INACTIVE.toObjectCode());
        return service;
    }

    private SubObjectCodeService buildMockSubObjectCodeService() {
        SubObjectCodeService service = Mockito.mock(SubObjectCodeService.class);
        Mockito.when(service.getByPrimaryIdForCurrentYear(SubObjectCodeEnum.VALID.chart, 
                SubObjectCodeEnum.VALID.account, SubObjectCodeEnum.VALID.objectCode, SubObjectCodeEnum.VALID.subObjectCode))
                .thenReturn(SubObjectCodeEnum.VALID.toSubObjectCode());
        Mockito.when(service.getByPrimaryIdForCurrentYear(SubObjectCodeEnum.INACTIVE.chart,
                SubObjectCodeEnum.INACTIVE.account, SubObjectCodeEnum.INACTIVE.objectCode,
                SubObjectCodeEnum.INACTIVE.subObjectCode)).thenReturn(SubObjectCodeEnum.INACTIVE.toSubObjectCode());
        return service;
    }

    private ProjectCodeService buildMockProjectCodeService() {
        ProjectCodeService service = Mockito.mock(ProjectCodeService.class);
        Mockito.when(service.getByPrimaryId(ProjectCodeEnum.VALID.projectCode))
                .thenReturn(ProjectCodeEnum.VALID.toProjectCode());
        Mockito.when(service.getByPrimaryId(ProjectCodeEnum.INACTIVE.projectCode))
                .thenReturn(ProjectCodeEnum.INACTIVE.toProjectCode());
        return service;
    }

    private ProjectCode createProjectCode(String projectCode, boolean active) {
        ProjectCode project = new ProjectCode();
        project.setCode(projectCode);
        project.setActive(active);
        return project;
    }

    private SubAccountService buildMockSubAccountService() {
        SubAccountService service = Mockito.mock(SubAccountService.class);
        Mockito.when(service.getByPrimaryId(SubAccountEnum.VALID.chart, SubAccountEnum.VALID.account,
                SubAccountEnum.VALID.subAccount)).thenReturn(SubAccountEnum.VALID.toSubAccount());
        Mockito.when(service.getByPrimaryId(SubAccountEnum.INACTIVE.chart, SubAccountEnum.INACTIVE.account,
                SubAccountEnum.INACTIVE.subAccount)).thenReturn(SubAccountEnum.INACTIVE.toSubAccount());
        return service;
    }

    @AfterEach
    public void tearDown() {
        concurAccountValidationService = null;
    }

    @ParameterizedTest
    @MethodSource("testCheckAccountParams")
    public void testCheckAccount(AccountEnum accountEnum, boolean validationExpectation, String errorMessageKey,
            String detailMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        List<String> expectedDetailMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(ConcurAccountValidationTestConstants.buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, accountEnum.chart, accountEnum.account));
        } else {
            String accountDetailMessage = MessageFormat.format(
                    ConcurAccountValidationTestConstants.buildMockConfigurationService()
                            .getPropertyValueAsString(detailMessageKey),
                    accountEnum.chart, accountEnum.account, accountEnum.subFundGroupCode,
                    accountEnum.higherEdFunctionCode);

            expectedDetailMessages.add(accountDetailMessage);
        }
        ValidationResult validationResult = concurAccountValidationService.checkAccount(accountEnum.chart,
                accountEnum.account);
        validateResults(validationExpectation, expectedErrorMessages, expectedDetailMessages, validationResult,
                "testCheckAccount");
    }

    private static Stream<Arguments> testCheckAccountParams() {
        return Stream.of(
                Arguments.of(AccountEnum.VALID, true, null,
                        ConcurKeyConstants.MESSAGE_CONCUR_EVENT_NOTIFICATION_ACCOUNT_DETAIL),
                Arguments.of(AccountEnum.NULL, false, KFSKeyConstants.ERROR_EXISTENCE, null),
                Arguments.of(AccountEnum.BAD, false, KFSKeyConstants.ERROR_EXISTENCE, null),
                Arguments.of(AccountEnum.INACTIVE, false, KFSKeyConstants.ERROR_INACTIVE, null));
    }

    private void validateResults(boolean validationExpectation, List<String> expectedErrorMessages,
            ValidationResult validationResult, String functionName) {
        validateResults(validationExpectation, expectedErrorMessages, new ArrayList<String>(), validationResult,
                functionName);
    }

    private void validateResults(boolean validationExpectation, List<String> expectedErrorMessages,
            List<String> expectedDetailMessages, ValidationResult validationResult, String functionName) {
        logMessagesDetails(validationResult, expectedErrorMessages, expectedDetailMessages, functionName);

        Assert.assertEquals("Validation was expected to be " + validationExpectation, validationExpectation,
                validationResult.isValid());
        Assert.assertEquals("Number of error messages is not what is exptected", expectedErrorMessages.size(),
                validationResult.getErrorMessages().size());
        Assert.assertEquals("Number of detail messages is not what is exptected", expectedDetailMessages.size(),
                validationResult.getAccountDetailMessages().size());

        for (String expectedMessage : expectedErrorMessages) {
            boolean isExpectedMessageFound = validationResult.getErrorMessages().contains(expectedMessage);
            Assert.assertTrue("expected to find error " + expectedMessage, isExpectedMessageFound);
        }

        for (String expectedMessage : expectedDetailMessages) {
            boolean isExpectedMessageFound = validationResult.getAccountDetailMessages().contains(expectedMessage);
            Assert.assertTrue("expected to find account detail " + expectedMessage, isExpectedMessageFound);
        }

        String expectedFormatedString = StringUtils.EMPTY;
        for (String expectedMessage : expectedErrorMessages) {
            expectedFormatedString = expectedFormatedString + expectedMessage + KFSConstants.NEWLINE;
        }
        Assert.assertEquals(expectedFormatedString, validationResult.getErrorMessagesAsOneFormattedString());
    }

    private void logMessagesDetails(ValidationResult validationResult, List<String> expectedErrorMessages,
            List<String> expectedDetailMessages, String functionName) {
        if (LOG.isDebugEnabled()) {
            for (String expectedMessage : expectedErrorMessages) {
                LOG.debug(functionName + ", expected error message: " + expectedMessage);
            }
            for (String actualMessage : validationResult.getErrorMessages()) {
                LOG.debug(functionName + ", actual error message:   " + actualMessage);
            }
            for (String expectedMessage : expectedDetailMessages) {
                LOG.debug(functionName + ", expected detail message: " + expectedMessage);
            }
            for (String expectedMessage : validationResult.getAccountDetailMessages()) {
                LOG.debug(functionName + ", actual detail message:   " + expectedMessage);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("testCheckObjectCodeParams")
    public void testCheckObjectCode(ObjectCodeEnum objectCodeEnum, boolean validationExpectation,
            String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(ConcurAccountValidationTestConstants.buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, objectCodeEnum.chart,
                    objectCodeEnum.objectCode));
        }
        ValidationResult validationResult = concurAccountValidationService.checkObjectCode(objectCodeEnum.chart,
                objectCodeEnum.objectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }

    private static Stream<Arguments> testCheckObjectCodeParams() {
        return Stream.of(Arguments.of(ObjectCodeEnum.VALID, true, null),
                Arguments.of(ObjectCodeEnum.NULL, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ObjectCodeEnum.BAD, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(ObjectCodeEnum.INACTIVE, false, KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testCheckSubAccountParams")
    public void testCheckSubAccount(SubAccountEnum subAccountEnum, boolean validationExpectation,
            String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(ConcurAccountValidationTestConstants.buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, subAccountEnum.chart,
                    subAccountEnum.account, subAccountEnum.subAccount));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubAccount(subAccountEnum.chart,
                subAccountEnum.account, subAccountEnum.subAccount);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testCheckObjectCode");
    }

    private static Stream<Arguments> testCheckSubAccountParams() {
        return Stream.of(Arguments.of(SubAccountEnum.VALID, true, null),
                Arguments.of(SubAccountEnum.NULL_SUB_ACCOUNT, true, null),
                Arguments.of(SubAccountEnum.BAD_SUB_ACCOUNT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(SubAccountEnum.BAD_ACCOUNT, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(SubAccountEnum.INACTIVE, false, KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testcheckSubObjectCodeParams")
    public void testcheckSubObjectCode(SubObjectCodeEnum subObjectCodeEnum, boolean validationExpectation,
            String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(ConcurAccountValidationTestConstants.buildFormattedMessage(errorMessageKey,
                    ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, subObjectCodeEnum.chart,
                    subObjectCodeEnum.account, subObjectCodeEnum.objectCode, subObjectCodeEnum.subObjectCode));
        }
        ValidationResult validationResult = concurAccountValidationService.checkSubObjectCode(subObjectCodeEnum.chart,
                subObjectCodeEnum.account, subObjectCodeEnum.objectCode, subObjectCodeEnum.subObjectCode);
        validateResults(validationExpectation, expectedErrorMessages, validationResult, "testcheckSubObjectCode");
    }

    private static Stream<Arguments> testcheckSubObjectCodeParams() {
        return Stream.of(Arguments.of(SubObjectCodeEnum.VALID, true, null),
                Arguments.of(SubObjectCodeEnum.BAD_SUB_OBJ, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(SubObjectCodeEnum.NULL_SUB_OBJ, true, null),
                Arguments.of(SubObjectCodeEnum.BAD_OBJ, false, KFSKeyConstants.ERROR_EXISTENCE),
                Arguments.of(SubObjectCodeEnum.INACTIVE, false, KFSKeyConstants.ERROR_INACTIVE));
    }

    @ParameterizedTest
    @MethodSource("testCheckProjectCodeParams")
    public void testCheckProjectCode(String projectCode, boolean validationExpectation, String errorMessageKey) {
        List<String> expectedErrorMessages = new ArrayList<>();
        if (!validationExpectation) {
            expectedErrorMessages.add(ConcurAccountValidationTestConstants.buildFormattedMessage(errorMessageKey,
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
    @EnumSource
    public void testValidateConcurAccountInfo(ConcurAccountInfoEnum accountinfo) {
        ConcurAccountInfo accountInfo = accountinfo.toConcurAccountInfo();
        ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(accountInfo);
        validateResults(accountinfo.validationExpectation, accountinfo.expectedErrorMessages, validationResult,
                "testValidateConcurAccountInfo");
    }
}
