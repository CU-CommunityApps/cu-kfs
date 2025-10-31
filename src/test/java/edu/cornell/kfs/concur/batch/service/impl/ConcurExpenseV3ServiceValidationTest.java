package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.fixture.ConcurExpenseV3AllocationFixture;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationWebApiService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.FixtureUtils;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurExpenseV3ServiceValidationTest {

    private static final String UNKNOWN = "Unknown";
    private static final String PENDING_EXTERNAL_VALIDATION = "Pending External Validation";
    private static final String DUMMY_TOKEN = "ABCDEFG1234567";
    private static final String DUMMY_REPORT_NUMBER = "77777";
    private static final String DUMMY_REPORT_NAME = "Test Report";
    private static final String DUMMY_NAME = "John Doe";
    private static final String DUMMY_EMAIL = "johndoe123@somewhere.edu";

    private enum TestAllocation {
        @ConcurExpenseV3AllocationFixture(
            expectValidationSuccess = false
        )
        ALLOCATION_WITH_EMPTY_ACCOUNTING_DATA,
        
        @ConcurExpenseV3AllocationFixture(
            chart = "IT",
            account = "G123456",
            objectCode = "3333",
            expectValidationSuccess = true
        )
        GOOD_ALLOCATION_1,

        @ConcurExpenseV3AllocationFixture(
            chart = "IT",
            account = "A555555",
            subAccount = "B7777",
            objectCode = "2468",
            subObject = "975",
            projectCode = "Z-THING",
            orgRefId = "ABC",
            expectValidationSuccess = true
        )
        GOOD_ALLOCATION_2,

        @ConcurExpenseV3AllocationFixture(
            subAccount = "B7777",
            subObject = "975",
            projectCode = "Z-THING",
            orgRefId = "ABC",
            expectValidationSuccess = false
        )
        ALLOCATION_MISSING_REQUIRED_DATA,

        @ConcurExpenseV3AllocationFixture(
            chart = UNKNOWN,
            account = "G123456",
            objectCode = "3333",
            expectValidationSuccess = false
        )
        ALLOCATION_WITH_INVALID_CHART,

        @ConcurExpenseV3AllocationFixture(
            chart = "IT",
            account = UNKNOWN,
            objectCode = "3333",
            expectValidationSuccess = false
        )
        ALLOCATION_WITH_INVALID_ACCOUNT,

        @ConcurExpenseV3AllocationFixture(
            chart = "IT",
            account = "G123456",
            objectCode = UNKNOWN,
            expectValidationSuccess = false
        )
        ALLOCATION_WITH_INVALID_OBJECT,

        @ConcurExpenseV3AllocationFixture(
            chart = "IT",
            account = "A555555",
            subAccount = "B7777",
            objectCode = "2468",
            subObject = UNKNOWN,
            projectCode = "Z-THING",
            orgRefId = "ABC",
            expectValidationSuccess = false
        )
        ALLOCATION_WITH_INVALID_SUB_OBJECT;
    }

    enum LocalTestCase {
        SINGLE_VALID_ALLOCATION(ConcurEventNotificationStatus.validAccounts,
                TestAllocation.GOOD_ALLOCATION_1),

        MULTIPLE_VALID_ALLOCATIONS(ConcurEventNotificationStatus.validAccounts,
                TestAllocation.GOOD_ALLOCATION_1,
                TestAllocation.GOOD_ALLOCATION_2),

        SINGLE_INVALID_ALLOCATION(ConcurEventNotificationStatus.invalidAccounts,
                TestAllocation.ALLOCATION_MISSING_REQUIRED_DATA),

        MULTIPLE_INVALID_ALLOCATIONS(ConcurEventNotificationStatus.invalidAccounts,
                TestAllocation.ALLOCATION_WITH_EMPTY_ACCOUNTING_DATA,
                TestAllocation.ALLOCATION_WITH_INVALID_CHART,
                TestAllocation.ALLOCATION_WITH_INVALID_ACCOUNT,
                TestAllocation.ALLOCATION_WITH_INVALID_OBJECT,
                TestAllocation.ALLOCATION_WITH_INVALID_SUB_OBJECT),

        VALID_INVALID_ALLOCATIONS_MIX(ConcurEventNotificationStatus.invalidAccounts,
                TestAllocation.GOOD_ALLOCATION_1,
                TestAllocation.ALLOCATION_WITH_INVALID_ACCOUNT,
                TestAllocation.GOOD_ALLOCATION_2),

        EMPTY_ALLOCATIONS_LIST(ConcurEventNotificationStatus.invalidAccounts);

        private final ConcurEventNotificationStatus expectedStatus;
        private final List<TestAllocation> allocations;

        private LocalTestCase(final ConcurEventNotificationStatus expectedStatus,
                final TestAllocation... allocations) {
            this.expectedStatus = expectedStatus;
            this.allocations = List.of(allocations);
        }
    }

    private ConcurExpenseV3ServiceImpl concurExpenseV3Service;

    @BeforeEach
    void setUp() throws Exception {
        concurExpenseV3Service = new ConcurExpenseV3ServiceImpl(new Environment("unittest", "prd", "Cornell"));
        concurExpenseV3Service.setConcurAccountValidationService(buildMockConcurAccountValidationService());
        concurExpenseV3Service.setConcurBatchUtilityService(buildMockConcurBatchUtilityService());
        concurExpenseV3Service.setConfigurationService(
                Mockito.mock(ConfigurationService.class));
        concurExpenseV3Service.setConcurEventNotificationWebApiService(
                Mockito.mock(ConcurEventNotificationWebApiService.class));
    }

    @AfterEach
    void tearDown() throws Exception {
        concurExpenseV3Service = null;
    }

    private ConcurAccountValidationService buildMockConcurAccountValidationService() {
        return new CuMockBuilder<>(ConcurAccountValidationService.class)
                .withAnswer(service -> service.validateConcurAccountInfo(Mockito.any()),
                        invocation -> performMockValidationOfAccountInfo(invocation.getArgument(0)))
                .build();
    }

    private ConcurAccountValidationService buildMockConcurAccountValidationServiceAlwaysThrowingExceptions() {
        return new CuMockBuilder<>(ConcurAccountValidationService.class)
                .withAnswer(service -> service.validateConcurAccountInfo(Mockito.any()),
                        invocation -> {
                            throw new RuntimeException("Forcing RuntimeException");
                        }
                )
                .build();
    }

    private ValidationResult performMockValidationOfAccountInfo(final ConcurAccountInfo accountInfo) {
        final ValidationResult result = new ValidationResult();
        if (StringUtils.isAnyBlank(accountInfo.getChart(), accountInfo.getAccountNumber(),
                accountInfo.getObjectCode())) {
            final String messageSuffix = accountInfo.toString().replace(KFSConstants.NEWLINE, " /// ");
            result.setValid(false);
            result.addErrorMessage("One or more required account info fields is missing: " + messageSuffix);
        } else if (StringUtils.equalsAnyIgnoreCase(UNKNOWN, accountInfo.getChart(), accountInfo.getAccountNumber(),
                accountInfo.getSubAccountNumber(), accountInfo.getObjectCode(), accountInfo.getSubObjectCode(),
                accountInfo.getProjectCode())) {
            final String messageSuffix = accountInfo.toString().replace(KFSConstants.NEWLINE, " /// ");
            result.setValid(false);
            result.addErrorMessage("One or more account info fields was marked as referring to a nonexistent value: "
                    + messageSuffix);
        }
        return result;
    }

    private ConcurBatchUtilityService buildMockConcurBatchUtilityService() {
        return new CuMockBuilder<>(ConcurBatchUtilityService.class)
                .withReturn(
                        service -> service.getConcurParameterValue(
                                ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND),
                        KRADConstants.NO_INDICATOR_VALUE)
                .build();
    }

    @ParameterizedTest
    @EnumSource
    void testExpenseAllocationValidation(final LocalTestCase testCase) throws Exception {
        assertAccountValidationHasExpectedOutcome(testCase.expectedStatus, testCase);
    }

    @ParameterizedTest
    @EnumSource
    void testExpenseAllocationValidationWithForcedException(final LocalTestCase testCase) throws Exception {
        concurExpenseV3Service.setConcurAccountValidationService(
                buildMockConcurAccountValidationServiceAlwaysThrowingExceptions());
        final ConcurEventNotificationStatus expectedStatus = testCase.allocations.isEmpty()
                ? ConcurEventNotificationStatus.invalidAccounts
                : ConcurEventNotificationStatus.processingError;
        assertAccountValidationHasExpectedOutcome(expectedStatus, testCase);
    }

    private void assertAccountValidationHasExpectedOutcome(final ConcurEventNotificationStatus expectedStatus,
            final LocalTestCase testCase) throws Exception {
        final List<ConcurExpenseV3AllocationFixture> allocationFixtures = testCase.allocations.stream()
                .map(fixture -> FixtureUtils.getAnnotationBasedFixture(fixture, ConcurExpenseV3AllocationFixture.class))
                .collect(Collectors.toUnmodifiableList());
        final List<ConcurExpenseAllocationV3ListItemDTO> allocations = allocationFixtures.stream()
                .map(ConcurExpenseV3AllocationFixture.Utils::toAllocationDto)
                .collect(Collectors.toUnmodifiableList());
        final int expectedErrorCount = getExpectedMockErrorMessageCount(expectedStatus, allocationFixtures);

        final List<ConcurEventNotificationResponse> processingResults = new ArrayList<>();
        concurExpenseV3Service.validateExpenseAllocations(DUMMY_TOKEN, processingResults, allocations,
                DUMMY_REPORT_NUMBER, DUMMY_REPORT_NAME, PENDING_EXTERNAL_VALIDATION, DUMMY_NAME, DUMMY_EMAIL);
        assertAccountValidationReturnsExpectedResponse(expectedStatus, expectedErrorCount, processingResults);
    }

    private void assertAccountValidationReturnsExpectedResponse(
            final ConcurEventNotificationStatus expectedStatus, final int expectedErrorCount,
            final List<ConcurEventNotificationResponse> processingResults) throws Exception {
        assertEquals(1, processingResults.size(), "Wrong number of processing results");
        final ConcurEventNotificationResponse response = processingResults.get(0);
        assertNotNull(response, "Processing result/response object should not have been null");
        assertEquals(expectedStatus, response.getEventNotificationStatus(), "Wrong validation status/result");
        assertEquals(expectedErrorCount, response.getErrorMessages().size(), "Wrong number of error messages");
    }

    private int getExpectedMockErrorMessageCount(final ConcurEventNotificationStatus expectedStatus,
            final List<ConcurExpenseV3AllocationFixture> allocationFixtures) {
        if (allocationFixtures.isEmpty()) {
            return 1;
        } else if (expectedStatus == ConcurEventNotificationStatus.processingError) {
            return 1;
        } else {
            return (int) allocationFixtures.stream()
                    .filter(fixture -> !fixture.expectValidationSuccess())
                    .count();
        }
    }

}
