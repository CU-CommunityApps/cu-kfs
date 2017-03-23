package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurCollectorBatchFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEFileFixture;

public class ConcurStandardAccountingExtractCollectorBatchBuilderTest {

    protected static final int MIN_YEAR = 2000;

    protected TestConcurStandardAccountingExtractCollectorBatchBuilder builder;
    protected DateTimeService dateTimeService;

    @Before
    public void setUp() throws Exception {
        dateTimeService = new DateTimeServiceImpl();
        builder = new TestConcurStandardAccountingExtractCollectorBatchBuilder(
                this::getFiscalYear, dateTimeService::toString, this::validateLine, this::getParameterValue);
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

    protected void assertCollectorBatchIsBuiltProperly(
            ConcurCollectorBatchFixture expectedFixture, ConcurSAEFileFixture fixtureToBuildFrom) throws Exception {
        CollectorBatch expected = expectedFixture.toCollectorBatch();
        ConcurStandardAccountingExtractFile saeFileContents = fixtureToBuildFrom.toExtractFile();
        CollectorBatch actual = builder.buildCollectorBatchFromStandardAccountingExtract(1, saeFileContents);
        
        assertNotNull("The SAE file contents should have been converted to a CollectorBatch successfully", actual);
        assertCollectorBatchHasCorrectData(expected, actual);
    }

    protected void assertCollectorBatchHasCorrectData(CollectorBatch expected, CollectorBatch actual) throws Exception {
        assertEquals("Wrong record type", expected.getRecordType(), actual.getRecordType());
        assertEquals("Wrong fiscal year", expected.getUniversityFiscalYear(), actual.getUniversityFiscalYear());
        assertEquals("Wrong chart code", expected.getChartOfAccountsCode(), actual.getChartOfAccountsCode());
        assertEquals("Wrong org code", expected.getOrganizationCode(), actual.getOrganizationCode());
        assertEquals("Wrong transmission date", expected.getTransmissionDate(), actual.getTransmissionDate());
        assertEquals("Wrong sequence number", expected.getBatchSequenceNumber(), actual.getBatchSequenceNumber());
        assertEquals("Wrong campus code", expected.getCampusCode(), actual.getCampusCode());
        assertEquals("Wrong mailing address", expected.getMailingAddress(), actual.getMailingAddress());
        assertEquals("Wrong email address", expected.getEmailAddress(), actual.getEmailAddress());
        assertEquals("Wrong user", expected.getPersonUserID(), actual.getPersonUserID());
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

    // TODO: See if this can be replaced by the validation service once Jay's changes are merged!
    protected boolean validateLine(ConcurStandardAccountingExtractDetailLine saeLine) {
        if (StringUtils.isBlank(saeLine.getReportId()) || StringUtils.isBlank(saeLine.getJournalAccountCode())) {
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
            default :
                return null;
        }
    }

    /**
     * Test-only builder subclass that uses hard-coded versions of certain dash-only codes/numbers.
     */
    public static class TestConcurStandardAccountingExtractCollectorBatchBuilder
            extends ConcurStandardAccountingExtractCollectorBatchBuilder {
        
        public TestConcurStandardAccountingExtractCollectorBatchBuilder(
                Function<Date,Integer> fiscalYearFinder, BiFunction<Date,String,String> dateFormatter,
                Predicate<ConcurStandardAccountingExtractDetailLine> saeLineValidator, Function<String,String> parameterFinder) {
            super(fiscalYearFinder, dateFormatter, saeLineValidator, parameterFinder);
        }
        
        @Override
        protected String getDashSubAccountNumber() {
            return ConcurTestConstants.DASH_SUB_ACCOUNT_NUMBER;
        }
        
        @Override
        protected String getDashSubObjectCode() {
            return ConcurTestConstants.DASH_SUB_OBJECT_CODE;
        }
        
        @Override
        protected String getDashProjectCode() {
            return ConcurTestConstants.DASH_PROJECT_CODE;
        }
    }

}
