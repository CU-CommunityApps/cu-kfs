package edu.cornell.kfs.fp.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;
import edu.cornell.kfs.fp.dataaccess.RecurringDisbursementVoucherSearchDao;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentReportService;
import edu.cornell.kfs.fp.service.impl.fixture.RcdvSpawnedDisbursementVoucherFixture;
import edu.cornell.kfs.sys.util.MockPersonUtil;

@Execution(ExecutionMode.SAME_THREAD)
public class RecurringDisbursementVoucherDocumentServiceDvRoutingTest {

    private static final String MOCK_REPORT_FILE_PATH = "test/mockReport.txt";
    private static final Pattern UNRESOLVED_PLACEHOLDERS_PATTERN = Pattern.compile("\\{[0-9]+\\}");

    private RecurringDisbursementVoucherDocumentServiceImpl rcdvDocumentService;
    private Map<String, RcdvSpawnedDisbursementVoucherFixture> spawnedDvs;
    private List<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItems;
    private Map<String, String> messageProperties;

    @BeforeEach
    public void setUp() throws Exception {
        spawnedDvs = new LinkedHashMap<>();
        reportItems = new ArrayList<>();
        messageProperties = buildMessageProperties();
        RecurringDisbursementVoucherSearchDao rcdvSearchDao = buildMockRcdvSearchDao();
        TestRecurringDisbursementVoucherDocumentRoutingServiceImpl rcdvDocumentRoutingService =
                buildRcdvDocumentRoutingService(rcdvSearchDao);
        rcdvDocumentService = buildRcdvDocumentService(rcdvSearchDao, rcdvDocumentRoutingService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        spawnedDvs = null;
        reportItems = null;
        messageProperties = null;
        rcdvDocumentService = null;
    }

    private Map<String, String> buildMessageProperties() {
        return Map.ofEntries(
                Map.entry(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_EXPIRED,
                        "Account {0} has expired.  Please override to use it anyway, or use the recommended " +
                                "continuation account {1} {2}, or use a different account."),
                Map.entry(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_CLOSED_WITH_IDENTIFYING_ACCOUNTING_LINE,
                        "{0} The specified {1} is closed."),
                Map.entry(KFSKeyConstants.Bank.ERROR_DISBURSEMENT_NOT_SUPPORTED,
                        "Bank code does not support disbursements.")
        );
    }

    private RecurringDisbursementVoucherSearchDao buildMockRcdvSearchDao() {
        RecurringDisbursementVoucherSearchDao searchDao = Mockito.mock(RecurringDisbursementVoucherSearchDao.class);
        Mockito.when(searchDao.findSavedDvIdsSpawnedByRecurringDvForCurrentAndPastFiscalPeriods(Mockito.any()))
                .then(this::findSpawnedDvs);
        Mockito.when(searchDao.findRecurringDvIdForSpawnedDv(Mockito.anyString()))
                .then(this::findRecurringDvId);
        return searchDao;
    }

    private Collection<String> findSpawnedDvs(InvocationOnMock invocation) {
        return spawnedDvs.values().stream()
                .map(RcdvSpawnedDisbursementVoucherFixture::getDocumentNumber)
                .collect(Collectors.toUnmodifiableList());
    }

    private String findRecurringDvId(InvocationOnMock invocation) {
        String spawnedDvId = invocation.getArgument(0);
        return findDvFixtureByDocumentNumber(spawnedDvId)
                .map(RcdvSpawnedDisbursementVoucherFixture::getRcdvDocumentNumber)
                .orElse(null);
    }

    private Optional<RcdvSpawnedDisbursementVoucherFixture> findDvFixtureByDocumentNumber(String documentNumber) {
        return Optional.ofNullable(spawnedDvs.get(documentNumber));
    }

    private RecurringDisbursementVoucherDocumentServiceImpl buildRcdvDocumentService(
            RecurringDisbursementVoucherSearchDao rcdvSearchDao,
            TestRecurringDisbursementVoucherDocumentRoutingServiceImpl rcdvDocumentRoutingService) {
        RecurringDisbursementVoucherDocumentServiceImpl rcdvDocumentService =
                new RecurringDisbursementVoucherDocumentServiceImpl();
        rcdvDocumentService.setRecurringDisbursementVoucherSearchDao(rcdvSearchDao);
        rcdvDocumentService.setRecurringDisbursementVoucherDocumentRoutingService(rcdvDocumentRoutingService);
        rcdvDocumentService.setRecurringDisbursementVoucherDocumentReportService(buildMockRcdvDocumentReportService());
        rcdvDocumentService.setAccountingPeriodService(buildMockAccountingPeriodService());
        return rcdvDocumentService;
    }

    private RecurringDisbursementVoucherDocumentReportService buildMockRcdvDocumentReportService() {
        RecurringDisbursementVoucherDocumentReportService reportService = Mockito.mock(
                RecurringDisbursementVoucherDocumentReportService.class);
        Mockito.when(reportService.buildDvAutoApproveErrorReport(Mockito.any()))
                .thenReturn(new File(MOCK_REPORT_FILE_PATH));
        Mockito.doNothing()
                .when(reportService).sendDvAutoApproveErrorReportEmail(Mockito.any());
        return reportService;
    }

    private AccountingPeriodService buildMockAccountingPeriodService() {
        LocalDate march_31_2024 = LocalDate.of(2024, 3, 31);
        AccountingPeriod march2024Period = new AccountingPeriod();
        march2024Period.setUniversityFiscalPeriodEndDate(java.sql.Date.valueOf(march_31_2024));

        AccountingPeriodService accountingPeriodService = Mockito.mock(AccountingPeriodService.class);
        Mockito.when(accountingPeriodService.getByDate(Mockito.any()))
                .thenReturn(march2024Period);
        return accountingPeriodService;
    }

    private TestRecurringDisbursementVoucherDocumentRoutingServiceImpl buildRcdvDocumentRoutingService(
            RecurringDisbursementVoucherSearchDao rcdvSearchDao) {
        TestRecurringDisbursementVoucherDocumentRoutingServiceImpl routingService =
                new TestRecurringDisbursementVoucherDocumentRoutingServiceImpl(this::addReportItem);
        routingService.setRecurringDisbursementVoucherSearchDao(rcdvSearchDao);
        routingService.setDocumentService(buildMockDocumentService());
        routingService.setPersonService(buildMockPersonService());
        routingService.setConfigurationService(buildMockConfigurationService());
        return routingService;
    }

    private void addReportItem(RecurringDisbursementVoucherDocumentRoutingReportItem reportItem) {
        reportItems.add(reportItem);
    }

    private DocumentService buildMockDocumentService() {
        DocumentService documentService = Mockito.mock(DocumentService.class);
        Mockito.when(documentService.getByDocumentHeaderId(Mockito.anyString()))
                .then(this::getDvDocumentById);
        Mockito.when(documentService.blanketApproveDocument(Mockito.any(), Mockito.anyString(), Mockito.any()))
                .then(this::handleDvBlanketApprove);
        return documentService;
    }

    private CuDisbursementVoucherDocument getDvDocumentById(InvocationOnMock invocation) {
        String documentNumber = invocation.getArgument(0);
        return findDvFixtureByDocumentNumber(documentNumber)
                .map(RcdvSpawnedDisbursementVoucherFixture::toDvDocument)
                .orElse(null);
    }

    private CuDisbursementVoucherDocument handleDvBlanketApprove(InvocationOnMock invocation) {
        CuDisbursementVoucherDocument document = invocation.getArgument(0);
        RcdvSpawnedDisbursementVoucherFixture fixture = findDvFixtureByDocumentNumber(document.getDocumentNumber())
                .orElseThrow(() -> new RuntimeException("DV fixture not found"));
        if (fixture.forceRoutingError) {
            throw new RuntimeException("Forcing unexpected routing error");
        }
        List<ErrorMessage> validationErrors = fixture.getValidationErrors();
        if (!validationErrors.isEmpty()) {
            for (ErrorMessage validationError : validationErrors) {
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, validationError.getErrorKey(),
                        validationError.getMessageParameters());
            }
            throw new ValidationException("business rule validation failed");
        }
        return document;
    }

    private PersonService buildMockPersonService() {
        Person mockSystemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        PersonService personService = Mockito.mock(PersonService.class);
        Mockito.when(personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER))
                .thenReturn(mockSystemUser);
        return personService;
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(Mockito.any()))
                .then(invocation -> messageProperties.get(invocation.getArgument(0)));
        return configurationService;
    }



    static Stream<Arguments> dvAutoApprovalTestCases() {
        return Stream.of(
                dvTest("No DVs (Empty)"),
                dvTest("Single Valid DV",
                        RcdvSpawnedDisbursementVoucherFixture.DV_VALID),
                dvTest("Single DV with Expired Account",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_EXPIRED_ACCOUNT),
                dvTest("Single DV with Closed Account",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_CLOSED_ACCOUNT),
                dvTest("Single DV with Invalid Disbursement Bank",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_INVALID_BANK),
                dvTest("Single DV with Multiple Validation Errors",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_MULTIPLE),
                dvTest("Single DV with Invalid Auto-Approve Initiator",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_UNEXPECTED_INITIATOR),
                dvTest("Single DV Configured to Encounter a Routing Error",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_ROUTING_FAILURE),
                dvTest("Multiple Valid DVs",
                        RcdvSpawnedDisbursementVoucherFixture.DV_VALID,
                        RcdvSpawnedDisbursementVoucherFixture.DV_VALID_2),
                dvTest("Multiple Invalid DVs",
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_EXPIRED_ACCOUNT,
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_MULTIPLE,
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_ROUTING_FAILURE),
                dvTest("Multiple Valid and Invalid DVs",
                        RcdvSpawnedDisbursementVoucherFixture.DV_VALID,
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_CLOSED_ACCOUNT,
                        RcdvSpawnedDisbursementVoucherFixture.DV_VALID_2,
                        RcdvSpawnedDisbursementVoucherFixture.DV_ERROR_INVALID_BANK)
        );
    }

    private static Arguments dvTest(String description, RcdvSpawnedDisbursementVoucherFixture... fixtures) {
        return Arguments.of(Named.of(description, List.of(fixtures)));
    }

    @ParameterizedTest()
    @MethodSource("dvAutoApprovalTestCases")
    void testAutoApprovalOfSpawnedDvs(List<RcdvSpawnedDisbursementVoucherFixture> dvFixtures) throws Exception {
        for (RcdvSpawnedDisbursementVoucherFixture dvFixture : dvFixtures) {
            spawnedDvs.put(dvFixture.documentNumber, dvFixture);
        }
        assertEquals(dvFixtures.size(), spawnedDvs.size(),
                "Wrong number of doc-ID-keyed DV fixtures; there may have been multiple fixtures with the same ID");
        rcdvDocumentService.autoApproveDisbursementVouchersSpawnedByRecurringDvs();
        assertDvAutoApprovalHasExpectedResults();
    }

    private void assertDvAutoApprovalHasExpectedResults() {
        assertEquals(spawnedDvs.size(), reportItems.size(), "Wrong number of DV processing results");
        for (RecurringDisbursementVoucherDocumentRoutingReportItem reportItem : reportItems) {
            String documentNumber = reportItem.getSpawnedDvDocumentNumber();
            assertTrue(StringUtils.isNotBlank(documentNumber), "Report item should not have a blank Document ID");
            RcdvSpawnedDisbursementVoucherFixture dvFixture = findDvFixtureByDocumentNumber(documentNumber)
                    .orElse(null);
            assertNotNull(dvFixture, "Could not find DV fixture for Document " + documentNumber);
            assertEquals(dvFixture.getDocumentNumber(), documentNumber,
                    "Wrong DV fixture was retrieved for Document " + documentNumber);
            assertEquals(dvFixture.getRcdvDocumentNumberForReportItem(), reportItem.getRecurringDvDocumentNumber(),
                    "Wrong RCDV Document ID detected in report item for DV Document " + documentNumber);

            List<String> errors = reportItem.getErrors();
            assertEquals(dvFixture.getExpectedErrorCount(), errors.size(),
                    "Wrong error count in report for Document " + documentNumber);
            for (String error : errors) {
                assertTrue(StringUtils.isNotBlank(error),
                        "Found an unexpected blank error message for Document " + documentNumber);
                assertFalse(UNRESOLVED_PLACEHOLDERS_PATTERN.matcher(error).find(),
                        "Found an unformatted/unresolved error message for Document " + documentNumber);
            }
        }
    }



    private static class TestRecurringDisbursementVoucherDocumentRoutingServiceImpl
            extends RecurringDisbursementVoucherDocumentRoutingServiceImpl {
        private final Consumer<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItemListener;
        private final Person mockSystemUser;

        private TestRecurringDisbursementVoucherDocumentRoutingServiceImpl(
                Consumer<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItemListener) {
            this.reportItemListener = reportItemListener;
            mockSystemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Override
        public RecurringDisbursementVoucherDocumentRoutingReportItem autoApproveSpawnedDisbursementVoucher(
                String spawnedDvDocumentNumber) {
            RecurringDisbursementVoucherDocumentRoutingReportItem reportItem =
                    super.autoApproveSpawnedDisbursementVoucher(spawnedDvDocumentNumber);
            reportItemListener.accept(reportItem);
            return reportItem;
        }

        @Override
        protected UserSession createUserSessionForSystemUser() {
            return MockPersonUtil.createMockUserSession(mockSystemUser);
        }

        @Override
        protected void setNotePostedTimestampToCurrent(Note note) {
            note.setNotePostedTimestamp(Timestamp.from(Instant.now()));
        }
    }

}
