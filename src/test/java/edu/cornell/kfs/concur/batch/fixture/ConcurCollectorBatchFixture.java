package edu.cornell.kfs.concur.batch.fixture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.mutable.MutableInt;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;

/**
 * Helper fixture for generating CollectorBatch business objects.
 * For convenience, one of the enum constructors can be used as a shortcut
 * for cases involving a fiscal year of 2017 and a transmission date of 01/04/2017.
 */
public enum ConcurCollectorBatchFixture {

    MERGING_TEST(1, 2, 225.00),
    UNIQUENESS_TEST(1, 16, 400.00),
    PAYMENT_CODE_TEST(1, 4, 100.00),
    VALIDATION_TEST(1, 2, 50.00),
    DEBIT_CREDIT_TEST(1, 8, 279.66),
    PENDING_CLIENT_TEST(1, 4, 110.00),
    FISCAL_YEAR_TEST1(1, 2, 50.00),
    FISCAL_YEAR_TEST2(1, 2016, "05/20/2016", 2, 50.00),
    FISCAL_YEAR_TEST3(1, 2017, "11/10/2016", 2, 50.00),
    FISCAL_YEAR_TEST4(1, 2016, "06/30/2016", 2, 50.00),
    FISCAL_YEAR_TEST5(1, 2017, "07/01/2016", 2, 50.00),
    DOCUMENT_NUMBER_TEST(1, 8, 200.00),
    EMPLOYEE_NAME_TEST(1, 8, 200.00),
    CASH_AND_CARD_TEST(1, 4, 105.00),
    CANCELED_TRIP_TEST(1, 2, 432.40);

    public final Integer batchSequenceNumber;
    public final String universityFiscalYear;
    public final String transmissionDate;
    public final Integer totalRecords;
    public final double totalAmount;

    private ConcurCollectorBatchFixture(int batchSequenceNumber, int totalRecords, double totalAmount) {
        this(batchSequenceNumber, ConcurTestConstants.FY_2017, ConcurTestConstants.JAN_04_2017,
                totalRecords, totalAmount);
    }

    private ConcurCollectorBatchFixture(int batchSequenceNumber, int universityFiscalYear,
            String transmissionDate, int totalRecords, double totalAmount) {
        this.batchSequenceNumber = Integer.valueOf(batchSequenceNumber);
        this.universityFiscalYear = Integer.toString(universityFiscalYear);
        this.transmissionDate = transmissionDate;
        this.totalRecords = Integer.valueOf(totalRecords);
        this.totalAmount = totalAmount;
    }

    public CollectorBatch toCollectorBatch() {
        CollectorBatch collectorBatch = toCollectorBatchWithoutOriginEntries();
        List<ConcurOriginEntryFixture> originEntryFixtures = ConcurFixtureUtils.getFixturesContainingParentFixture(
                ConcurOriginEntryFixture.class, this, ConcurOriginEntryFixture::getCollectorBatch);
        Map<String,MutableInt> nextSequenceNumbers = new HashMap<>();
        
        for (ConcurOriginEntryFixture originEntryFixture : originEntryFixtures) {
            OriginEntryFull originEntry = originEntryFixture.toOriginEntryFull();
            MutableInt nextSequenceNumber = nextSequenceNumbers.computeIfAbsent(
                    originEntry.getDocumentNumber(), (key) -> new MutableInt(0));
            nextSequenceNumber.increment();
            originEntry.setTransactionLedgerEntrySequenceNumber(nextSequenceNumber.toInteger());
            collectorBatch.addOriginEntry(originEntry);
        }
        
        return collectorBatch;
    }

    public CollectorBatch toCollectorBatchWithoutOriginEntries() {
        CollectorBatch collectorBatch = new CollectorBatch();
        
        collectorBatch.setBatchSequenceNumber(batchSequenceNumber);
        collectorBatch.setUniversityFiscalYear(universityFiscalYear);
        collectorBatch.setTransmissionDate(ConcurFixtureUtils.toSqlDate(transmissionDate));
        collectorBatch.setTotalRecords(totalRecords);
        collectorBatch.setTotalAmount(new KualiDecimal(totalAmount));
        collectorBatch.setChartOfAccountsCode(ParameterTestValues.COLLECTOR_CHART_CODE);
        collectorBatch.setOrganizationCode(ParameterTestValues.COLLECTOR_HIGHEST_LEVEL_ORG_CODE);
        collectorBatch.setCampusCode(ParameterTestValues.COLLECTOR_CAMPUS_CODE);
        collectorBatch.setMailingAddress(ParameterTestValues.COLLECTOR_CAMPUS_ADDRESS);
        collectorBatch.setDepartmentName(ParameterTestValues.COLLECTOR_DEPARTMENT_NAME);
        collectorBatch.setEmailAddress(ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_EMAIL);
        collectorBatch.setPersonUserID(ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_PERSON);
        collectorBatch.setPhoneNumber(ParameterTestValues.COLLECTOR_NOTIFICATION_CONTACT_PHONE);
        
        return collectorBatch;
    }

}
