package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurCollectorBatchFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.fixture.ConcurRequestedCashAdvanceFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEDetailLineFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEFileFixture;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractCollectorBatchBuilderTest {

    protected static final int MIN_YEAR = 2000;

    protected TestConcurStandardAccountingExtractCollectorBatchBuilder builder;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected OptionsService optionsService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;

    @Before
    public void setUp() throws Exception {
        concurRequestedCashAdvanceService = buildMockRequestedCashAdvanceService();
        concurStandardAccountingExtractCashAdvanceService = new ConcurStandardAccountingExtractCashAdvanceServiceImpl();
        configurationService = buildMockConfigurationService();
        concurBatchUtilityService = new ConcurBatchUtilityServiceImpl();
        optionsService = buildMockOptionsService();
        universityDateService = buildMockUniversityDateService();
        dateTimeService = new DateTimeServiceImpl();
        concurSAEValidationService = buildMockConcurSAEValidationService();
        builder = new TestConcurStandardAccountingExtractCollectorBatchBuilder(
                concurRequestedCashAdvanceService, concurStandardAccountingExtractCashAdvanceService, configurationService,
                concurBatchUtilityService, optionsService, universityDateService, dateTimeService, concurSAEValidationService, this::getParameterValue);
    }

    @Test
    public void testLinesAreMergedWhenKeyFieldsMatch() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.MERGING_TEST, ConcurSAEFileFixture.MERGING_TEST);
    }

    @Test
    public void testLinesRemainDistinctWhenKeyFieldsDiffer() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.UNIQUENESS_TEST, ConcurSAEFileFixture.UNIQUENESS_TEST);
    }

    @Test
    public void testProperReuseOfSameBuilderInstance() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.MERGING_TEST, ConcurSAEFileFixture.MERGING_TEST);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.MERGING_TEST, ConcurSAEFileFixture.MERGING_TEST);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.UNIQUENESS_TEST, ConcurSAEFileFixture.UNIQUENESS_TEST);
    }

    @Test
    public void testLineFilteringByPaymentCode() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.PAYMENT_CODE_TEST, ConcurSAEFileFixture.PAYMENT_CODE_TEST);
    }

    @Test
    public void testInvalidLinesAreSkipped() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.VALIDATION_TEST, ConcurSAEFileFixture.VALIDATION_TEST);
    }

    @Test
    public void testDebitCreditCalculations() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, ConcurSAEFileFixture.DEBIT_CREDIT_TEST);
    }

    @Test
    public void testLinesWithPendingClientObjectCodeAreHandledProperly() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.PENDING_CLIENT_TEST, ConcurSAEFileFixture.PENDING_CLIENT_TEST);
    }

    @Test
    public void testHandlingOfFiscalYearAndReportEndDate() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1, ConcurSAEFileFixture.FISCAL_YEAR_TEST1);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2, ConcurSAEFileFixture.FISCAL_YEAR_TEST2);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3, ConcurSAEFileFixture.FISCAL_YEAR_TEST3);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.FISCAL_YEAR_TEST4, ConcurSAEFileFixture.FISCAL_YEAR_TEST4);
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.FISCAL_YEAR_TEST5, ConcurSAEFileFixture.FISCAL_YEAR_TEST5);
    }

    @Test
    public void testGenerationOfDocumentNumberFromReportId() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST);
    }

    @Test
    public void testNameTruncatingForLineDescriptions() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST);
    }

    @Test
    public void testMixOfCashAndCorporateCardLinesForSameTransactionGrouping() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.CASH_AND_CARD_TEST, ConcurSAEFileFixture.CASH_AND_CARD_TEST);
    }

    @Test
    public void testTransactionGroupingWithNetCreditAmount() throws Exception {
        assertCollectorBatchIsBuiltProperly(ConcurCollectorBatchFixture.CANCELED_TRIP_TEST, ConcurSAEFileFixture.CANCELED_TRIP_TEST);
    }

    @Test
    public void testFullyUsedCashAdvance() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.FULL_USE_CASH_ADVANCE_TEST, ConcurSAEFileFixture.FULL_USE_CASH_ADVANCE_TEST);
    }

    @Test
    public void testPartiallyUsedCashAdvance() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.PARTIAL_USE_CASH_ADVANCE_TEST, ConcurSAEFileFixture.PARTIAL_USE_CASH_ADVANCE_TEST);
    }

    @Test
    public void testExpensesGreaterThanCashAdvanceAmount() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, ConcurSAEFileFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST);
    }

    @Test
    public void testCashAdvancesAcrossMultipleReportIds() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST);
    }

    @Test
    public void testExcludeRowsForReportIdWhenCashAdvanceObjectDoesNotExist() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.ORPHANED_CASH_ADVANCE_TEST, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST);
    }

    @Test
    public void testUniqueExpenseLinesPlusCashAdvanceWithAmountEqualToFirstExpense() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1, ConcurSAEFileFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1);
    }

    @Test
    public void testUniqueExpenseLinesPlusCashAdvanceWithAmountGreaterThanFirstExpense() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2, ConcurSAEFileFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2);
    }

    @Test
    public void testUniqueExpenseLinesPlusCashAdvanceWithAmountLessThanFirstExpense() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3, ConcurSAEFileFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3);
    }

    @Test
    public void testUniqueExpenseLinesPlusNonUniqueLinesPlusCashAdvance() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4, ConcurSAEFileFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4);
    }

    @Test
    public void testMultipleCashAdvancesWithSameReportEntryId() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST, ConcurSAEFileFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST);
    }

    @Test
    public void testCorpCardPersonalExpensesAndNoCashExpenses() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.PERSONAL_WITHOUT_CASH_TEST, ConcurSAEFileFixture.PERSONAL_WITHOUT_CASH_TEST);
    }

    @Test
    public void testCorpCardPersonalExpensesLessThanCashExpenses() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST, ConcurSAEFileFixture.CASH_EXCEEDS_PERSONAL_TEST);
    }

    @Test
    public void testCorpCardPersonalExpensesEqualToCashExpenses() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST);
    }

    @Test
    public void testCorpCardPersonalExpensesGreaterThanCashExpenses() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST);
    }

    @Test
    public void testCorpCardPersonalExpensesAndCashAdvancesInSameTrip() throws Exception {
        assertCollectorBatchIsBuiltProperly(
                ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST);
    }

    protected void assertCollectorBatchIsBuiltProperly(
            ConcurCollectorBatchFixture expectedFixture, ConcurSAEFileFixture fixtureToBuildFrom) throws Exception {
        CollectorBatch expected = expectedFixture.toCollectorBatch();
        List<ConcurSAEDetailLineFixture> lineFixtures = ConcurFixtureUtils.getFixturesContainingParentFixture(
                ConcurSAEDetailLineFixture.class, fixtureToBuildFrom, ConcurSAEDetailLineFixture::getExtractFile);
        
        ConcurStandardAccountingExtractFile saeFileContents = fixtureToBuildFrom.toExtractFile();
        ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
        CollectorBatch actual = builder.buildCollectorBatchFromStandardAccountingExtract(1, saeFileContents, reportData);
        
        assertNotNull("The SAE file contents should have been converted to a CollectorBatch successfully", actual);
        assertCollectorBatchHasCorrectData(expected, actual);
        
        assertReportHasCorrectErrorLinesAndOrdering(lineFixtures, reportData);
        assertReportHasCorrectPendingClientLinesAndOrdering(lineFixtures, reportData);
        assertReportHasCorrectCashAdvanceStatistics(lineFixtures, reportData);
        assertReportHasCorrectCorporateCardStatistics(lineFixtures, reportData);
        assertReportHasCorrectPseudoPaymentCodeStatistics(lineFixtures, reportData);
    }

    protected void assertCollectorBatchHasCorrectData(CollectorBatch expected, CollectorBatch actual) throws Exception {
        assertEquals("Wrong batch fiscal year", expected.getUniversityFiscalYear(), actual.getUniversityFiscalYear());
        assertEquals("Wrong batch chart code", expected.getChartOfAccountsCode(), actual.getChartOfAccountsCode());
        assertEquals("Wrong org code", expected.getOrganizationCode(), actual.getOrganizationCode());
        assertEquals("Wrong transmission date", expected.getTransmissionDate(), actual.getTransmissionDate());
        assertEquals("Wrong batch sequence number", expected.getBatchSequenceNumber(), actual.getBatchSequenceNumber());
        assertEquals("Wrong campus code", expected.getCampusCode(), actual.getCampusCode());
        assertEquals("Wrong mailing address", expected.getMailingAddress(), actual.getMailingAddress());
        assertEquals("Wrong email address", expected.getEmailAddress(), actual.getEmailAddress());
        assertEquals("Wrong user", expected.getPersonUserID(), actual.getPersonUserID());
        assertEquals("Wrong department name", expected.getDepartmentName(), actual.getDepartmentName());
        assertEquals("Wrong phone number", expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals("Wrong total record count", expected.getTotalRecords(), actual.getTotalRecords());
        assertEquals("Wrong total amount", expected.getTotalAmount(), actual.getTotalAmount());
        
        assertOriginEntriesHaveCorrectDataAndOrdering(expected.getOriginEntries(), actual.getOriginEntries());
    }

    protected void assertOriginEntriesHaveCorrectDataAndOrdering(
            List<OriginEntryFull> expectedEntries, List<OriginEntryFull> actualEntries) throws Exception {
        assertEquals("Wrong number of origin entries", expectedEntries.size(), actualEntries.size());
        
        for (int i = 0; i < expectedEntries.size(); i++) {
            assertOriginEntryHasCorrectData(expectedEntries.get(i), actualEntries.get(i));
        }
    }

    protected void assertOriginEntryHasCorrectData(OriginEntryFull expected, OriginEntryFull actual) throws Exception {
        assertEquals("Wrong balance type", expected.getFinancialBalanceTypeCode(), actual.getFinancialBalanceTypeCode());
        assertEquals("Wrong fiscal year", expected.getUniversityFiscalYear(), actual.getUniversityFiscalYear());
        assertEquals("Wrong chart code", expected.getChartOfAccountsCode(), actual.getChartOfAccountsCode());
        assertEquals("Wrong account number", expected.getAccountNumber(), actual.getAccountNumber());
        assertEquals("Wrong sub-account number", expected.getSubAccountNumber(), actual.getSubAccountNumber());
        assertEquals("Wrong object code", expected.getFinancialObjectCode(), actual.getFinancialObjectCode());
        assertEquals("Wrong sub-object code", expected.getFinancialSubObjectCode(), actual.getFinancialSubObjectCode());
        assertEquals("Wrong project code", expected.getProjectCode(), actual.getProjectCode());
        assertEquals("Wrong org ref ID", expected.getOrganizationReferenceId(), actual.getOrganizationReferenceId());
        assertEquals("Wrong doc type code", expected.getFinancialDocumentTypeCode(), actual.getFinancialDocumentTypeCode());
        assertEquals("Wrong origination code", expected.getFinancialSystemOriginationCode(), actual.getFinancialSystemOriginationCode());
        assertEquals("Wrong document number", expected.getDocumentNumber(), actual.getDocumentNumber());
        assertEquals("Wrong sequence number", expected.getTransactionLedgerEntrySequenceNumber(), actual.getTransactionLedgerEntrySequenceNumber());
        assertEquals("Wrong description", expected.getTransactionLedgerEntryDescription(), actual.getTransactionLedgerEntryDescription());
        assertEquals("Wrong transaction date", expected.getTransactionDate(), actual.getTransactionDate());
        assertEquals("Wrong debit/credit code", expected.getTransactionDebitCreditCode(), actual.getTransactionDebitCreditCode());
        assertEquals("Wrong transaction amount", expected.getTransactionLedgerEntryAmount(), actual.getTransactionLedgerEntryAmount());
    }

    protected void assertReportHasCorrectErrorLinesAndOrdering(
            List<ConcurSAEDetailLineFixture> lineFixtures, ConcurStandardAccountingExtractBatchReportData reportData) throws Exception {
        Set<String> reportIdsWithOrphanedCashAdvances = lineFixtures.stream()
                .filter(this::fixtureRepresentsOrphanedCashAdvanceLine)
                .map(ConcurSAEDetailLineFixture::getReportId)
                .collect(Collectors.toCollection(HashSet::new));
        
        assertReportHasCorrectLinesAndOrdering(lineFixtures,
                (fixture) -> fixtureFailsMinimalTestValidation(fixture) || reportIdsWithOrphanedCashAdvances.contains(fixture.reportId),
                reportData.getValidationErrorFileLines(), (expectedLine, actualLine) -> {});
    }

    protected void assertReportHasCorrectPendingClientLinesAndOrdering(
            List<ConcurSAEDetailLineFixture> lineFixtures, ConcurStandardAccountingExtractBatchReportData reportData) throws Exception {
        assertReportHasCorrectLinesAndOrdering(lineFixtures, this::fixtureRepresentsPendingClientLine,
                reportData.getPendingClientObjectCodeLines(), this::assertReportRowPropertiesForPendingClientItemsAreCorrect);
    } 

    protected <T extends ConcurBatchReportLineValidationErrorItem> void assertReportHasCorrectLinesAndOrdering(
            List<ConcurSAEDetailLineFixture> lineFixtures, Predicate<ConcurSAEDetailLineFixture> lineFilter, List<T> actualLines,
            BiConsumer<ConcurSAEDetailLineFixture,T> extraValidation) throws Exception {
        ConcurSAEDetailLineFixture[] expectedLines = lineFixtures.stream()
                .filter(lineFilter)
                .toArray(ConcurSAEDetailLineFixture[]::new);
        
        assertEquals("Wrong number of error-related lines in report", expectedLines.length, actualLines.size());
        
        int i = 0;
        for (ConcurSAEDetailLineFixture expectedLine : expectedLines) {
            T actualLine = actualLines.get(i);
            
            assertEquals("Wrong report ID", expectedLine.reportId, actualLine.getReportId());
            assertEquals("Wrong employee ID", expectedLine.employee.employeeId, actualLine.getEmployeeId());
            assertEquals("Wrong last name", expectedLine.employee.lastName, actualLine.getLastName());
            assertEquals("Wrong first name", expectedLine.employee.firstName, actualLine.getFirstName());
            assertEquals("Wrong middle initial", expectedLine.employee.middleInitial, actualLine.getMiddleInitial());
            
            List<String> messages = actualLine.getItemErrorResults();
            assertNotNull("Error messages list should not be null", messages);
            assertEquals("Wrong number of error messages in list", 1, messages.size());
            assertTrue("Error message should have been non-blank", StringUtils.isNotBlank(messages.get(0)));
            
            extraValidation.accept(expectedLine, actualLine);
            
            i++;
        }
    }

    protected void assertReportRowPropertiesForPendingClientItemsAreCorrect(
            ConcurSAEDetailLineFixture expectedLine, ConcurBatchReportMissingObjectCodeItem actualLine) {
        assertEquals("Wrong policy name", ConcurTestConstants.DEFAULT_POLICY_NAME, actualLine.getPolicyName());
        assertEquals("Wrong expense type name", ConcurTestConstants.DEFAULT_EXPENSE_TYPE_NAME, actualLine.getExpenseTypeName());
    }

    protected void assertReportHasCorrectCashAdvanceStatistics(List<ConcurSAEDetailLineFixture> lineFixtures,
            ConcurStandardAccountingExtractBatchReportData reportData) throws Exception {
        assertReportStatisticHasCorrectTotals(lineFixtures,
                this::fixtureRepresentsCashAdvanceLine, reportData.getCashAdvancesRelatedToExpenseReports());
    }

    protected void assertReportHasCorrectCorporateCardStatistics(List<ConcurSAEDetailLineFixture> lineFixtures,
            ConcurStandardAccountingExtractBatchReportData reportData) throws Exception {
        assertReportStatisticHasCorrectTotals(lineFixtures,
                this::fixtureRepresentsCorporateCardLine, reportData.getExpensesPaidOnCorporateCard());
    }

    protected void assertReportHasCorrectPseudoPaymentCodeStatistics(List<ConcurSAEDetailLineFixture> lineFixtures,
            ConcurStandardAccountingExtractBatchReportData reportData) throws Exception {
        assertReportStatisticHasCorrectTotals(lineFixtures,
                this::fixtureRepresentsPseudoPaymentCodeLine, reportData.getTransactionsBypassed());
    }

    protected void assertReportStatisticHasCorrectTotals(List<ConcurSAEDetailLineFixture> lineFixtures,
            Predicate<ConcurSAEDetailLineFixture> fixtureFilter, ConcurBatchReportSummaryItem actualSummary) throws Exception {
        ConcurSAEDetailLineFixture[] filteredItems = lineFixtures.stream()
                .filter(fixtureFilter)
                .toArray(ConcurSAEDetailLineFixture[]::new);
        KualiDecimal expectedAmount = Arrays.stream(filteredItems)
                .mapToDouble(ConcurSAEDetailLineFixture::getJournalAmount)
                .mapToObj(KualiDecimal::new)
                .reduce(KualiDecimal.ZERO, KualiDecimal::add);
        
        assertEquals("Wrong record count for statistic", filteredItems.length, actualSummary.getRecordCount());
        assertEquals("Wrong dollar amount for statistic", expectedAmount, actualSummary.getDollarAmount());
    }

    protected boolean fixtureFailsMinimalTestValidation(ConcurSAEDetailLineFixture fixture) {
        return !fixturePassesMinimalTestValidation(fixture);
    }

    protected boolean fixturePassesMinimalTestValidation(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.isNotBlank(fixture.reportId) && StringUtils.isNotBlank(fixture.journalAccountCode)
                && (fixtureRepresentsCashLine(fixture) || fixtureRepresentsCorporateCardLine(fixture));
    }

    protected boolean fixtureRepresentsCashAdvanceLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.isNotBlank(fixture.cashAdvanceKey);
    }

    protected boolean fixtureRepresentsOrphanedCashAdvanceLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.equals(ConcurTestConstants.CASH_ADVANCE_KEY_NONEXISTENT, fixture.cashAdvanceKey);
    }

    protected boolean fixtureRepresentsCashLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.equals(ConcurConstants.PAYMENT_CODE_CASH, fixture.paymentCode);
    }

    protected boolean fixtureRepresentsCorporateCardLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.equals(ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID, fixture.paymentCode);
    } 

    protected boolean fixtureRepresentsPseudoPaymentCodeLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.equals(ConcurConstants.PAYMENT_CODE_PSEUDO, fixture.paymentCode);
    }

    protected boolean fixtureRepresentsPendingClientLine(ConcurSAEDetailLineFixture fixture) {
        return StringUtils.equals(ConcurConstants.PENDING_CLIENT, fixture.journalAccountCode);
    }

    protected ConcurRequestedCashAdvanceService buildMockRequestedCashAdvanceService() {
        return buildMockService(ConcurRequestedCashAdvanceService.class, (service) -> {
            EasyMock.expect(
                    service.findConcurRequestedCashAdvanceByCashAdvanceKey(ConcurTestConstants.CASH_ADVANCE_KEY_1))
                    .andStubReturn(Collections.singletonList(
                            ConcurRequestedCashAdvanceFixture.CASH_ADVANCE_50.toRequestedCashAdvance()));
            EasyMock.expect(
                    service.findConcurRequestedCashAdvanceByCashAdvanceKey(ConcurTestConstants.CASH_ADVANCE_KEY_2))
                    .andStubReturn(Collections.singletonList(
                            ConcurRequestedCashAdvanceFixture.CASH_ADVANCE_200.toRequestedCashAdvance()));
            EasyMock.expect(
                    service.findConcurRequestedCashAdvanceByCashAdvanceKey(ConcurTestConstants.CASH_ADVANCE_KEY_NONEXISTENT))
                    .andStubReturn(Collections.emptyList());
        });
    }

    protected ConfigurationService buildMockConfigurationService() {
        return buildMockService(ConfigurationService.class, (service) -> {
            EasyMock.expect(
                    service.getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_GROUP_WITH_ORPHANED_CASH_ADVANCE))
                    .andStubReturn(PropertyTestValues.GROUP_WITH_ORPHANED_CASH_ADVANCE_MESSAGE);
            EasyMock.expect(
                    service.getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_ORPHANED_CASH_ADVANCE))
                    .andStubReturn(PropertyTestValues.ORPHANED_CASH_ADVANCE_MESSAGE);
        });
    }

    protected OptionsService buildMockOptionsService() {
        return buildMockService(OptionsService.class, (service) -> {
            SystemOptions testOptions = new SystemOptions();
            testOptions.setActualFinancialBalanceTypeCd(BalanceTypeService.ACTUAL_BALANCE_TYPE);
            
            EasyMock.expect(service.getOptions(EasyMock.anyObject()))
                    .andStubReturn(testOptions);
        });
    }

    protected UniversityDateService buildMockUniversityDateService() {
        return buildMockService(UniversityDateService.class, (service) -> {
            Capture<Date> dateArg = EasyMock.newCapture();
            EasyMock.expect(
                    service.getFiscalYear(
                            EasyMock.capture(dateArg)))
                    .andStubAnswer(() -> getFiscalYear(dateArg.getValue()));
        });
    }

    protected Integer getFiscalYear(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        DateTime dateTime = new DateTime(date);
        int year = dateTime.getYear();
        
        // For testing purposes, regard years prior to 2000 as not found.
        if (year < MIN_YEAR) {
            return null;
        }
        
        switch (dateTime.getMonthOfYear()) {
            case DateTimeConstants.JANUARY :
            case DateTimeConstants.FEBRUARY :
            case DateTimeConstants.MARCH :
            case DateTimeConstants.APRIL :
            case DateTimeConstants.MAY :
            case DateTimeConstants.JUNE :
                return Integer.valueOf(year);
            
            case DateTimeConstants.JULY :
            case DateTimeConstants.AUGUST :
            case DateTimeConstants.SEPTEMBER :
            case DateTimeConstants.OCTOBER :
            case DateTimeConstants.NOVEMBER :
            case DateTimeConstants.DECEMBER :
                return Integer.valueOf(year + 1);
            
            default :
                return null;
        }
    }

    protected ConcurStandardAccountingExtractValidationService buildMockConcurSAEValidationService() {
        return buildMockService(ConcurStandardAccountingExtractValidationService.class, (service) -> {
            Capture<ConcurStandardAccountingExtractDetailLine> saeLineArg = EasyMock.newCapture();
            EasyMock.expect(
                    service.validateConcurStandardAccountingExtractDetailLine(
                            EasyMock.capture(saeLineArg), EasyMock.anyObject()))
                    .andStubAnswer(() -> validateLine(saeLineArg.getValue()));
        });
    }

    protected boolean validateLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        if (StringUtils.isBlank(saeLine.getReportId()) || StringUtils.isBlank(saeLine.getJournalAccountCode())) {
            builder.reportUnprocessedLine(saeLine, "Line failed validation");
            return false;
        }
        
        return true;
    }

    protected String getParameterValue(String parameterName) {
        if (StringUtils.isBlank(parameterName)) {
            throw new IllegalArgumentException("parameterName cannot be blank");
        }
        
        switch (parameterName) {
            case ConcurParameterConstants.CONCUR_PENDING_CLIENT_OBJECT_CODE_OVERRIDE :
                return ConcurTestConstants.ParameterTestValues.OBJECT_CODE_OVERRIDE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DOCUMENT_TYPE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_DOCUMENT_TYPE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_SYSTEM_ORIGINATION_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_SYSTEM_ORIGINATION_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CHART_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_CHART_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_HIGHEST_LEVEL_ORG_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_HIGHEST_LEVEL_ORG_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_DEPARTMENT_NAME :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_DEPARTMENT_NAME;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_CAMPUS_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_CAMPUS_ADDRESS :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_CAMPUS_ADDRESS;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_EMAIL :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_EMAIL;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PERSON :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_PERSON;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_NOTIFICATION_CONTACT_PHONE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_PHONE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_CHART_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_PREPAID_OFFSET_CHART_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PREPAID_OFFSET_OBJECT_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_PREPAID_OFFSET_OBJECT_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PAYMENT_OFFSET_OBJECT_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_PAYMENT_OFFSET_OBJECT_CODE;
            case ConcurParameterConstants.CONCUR_SAE_COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE :
                return ConcurTestConstants.ParameterTestValues.COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE;
            default :
                return null;
        }
    }

    protected <T> T buildMockService(Class<T> serviceClass, Consumer<T> mockServiceConfigurer) {
        T mockService = EasyMock.createMock(serviceClass);
        mockServiceConfigurer.accept(mockService);
        EasyMock.replay(mockService);
        return mockService;
    }

    /**
     * Test-only builder subclass that uses hard-coded versions of certain dash-only codes/numbers.
     */
    public static class TestConcurStandardAccountingExtractCollectorBatchBuilder
            extends ConcurStandardAccountingExtractCollectorBatchBuilder {
        
        public TestConcurStandardAccountingExtractCollectorBatchBuilder(
                ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService,
                ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService,
                ConfigurationService configurationService, ConcurBatchUtilityService concurBatchUtilityService, OptionsService optionsService,
                UniversityDateService universityDateService, DateTimeService dateTimeService,
                ConcurStandardAccountingExtractValidationService concurSAEValidationService, Function<String,String> parameterFinder) {
            super(concurRequestedCashAdvanceService, concurStandardAccountingExtractCashAdvanceService, configurationService,
                    concurBatchUtilityService, optionsService, universityDateService, dateTimeService, concurSAEValidationService, parameterFinder);
        }
        
        @Override
        protected String getDashValueForProperty(String propertyName) {
            switch (propertyName) {
                case KFSPropertyConstants.SUB_ACCOUNT_NUMBER :
                    return ConcurTestConstants.DASH_SUB_ACCOUNT_NUMBER;
                case KFSPropertyConstants.SUB_OBJECT_CODE :
                    return ConcurTestConstants.DASH_SUB_OBJECT_CODE;
                case KFSPropertyConstants.PROJECT_CODE :
                    return ConcurTestConstants.DASH_PROJECT_CODE;
                default :
                    return StringUtils.EMPTY;
            }
        }
        
    }

}
