package edu.cornell.kfs.concur.batch.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

/**
 * Helper fixture for generating ConcurStandardAccountingExtractFile POJOs.
 * For convenience, one of the enum constructors can be used as a shortcut
 * for cases involving a batch date of 01/04/2017.
 * 
 * The POJOs will use the enum constant's name plus the ".txt" extension
 * as the originalFileName, for convenience purposes. Such filenames
 * are NOT expected to point to actual files; if there's a need to parse
 * an actual file and then compare the results using this enum,
 * then generating the filename some other way is recommended.
 */
public enum ConcurSAEFileFixture {

    MERGING_TEST(3, 225.00),
    UNIQUENESS_TEST(8, 400.00),
    PAYMENT_CODE_TEST(5, 250.00),
    VALIDATION_TEST(3, 150.00),
    DEBIT_CREDIT_TEST(6, 5.00),
    PENDING_CLIENT_TEST(3, 110.00),
    FISCAL_YEAR_TEST1(1, 50.00),
    FISCAL_YEAR_TEST2("05/20/2016", 1, 50.00),
    FISCAL_YEAR_TEST3("11/10/2016", 1, 50.00),
    FISCAL_YEAR_TEST4("06/30/2016", 1, 50.00),
    FISCAL_YEAR_TEST5("07/01/2016", 1, 50.00),
    DOCUMENT_NUMBER_TEST(4, 200.00),
    EMPLOYEE_NAME_TEST(4, 200.00),
    CASH_AND_CARD_TEST(2, 105.00),
    CANCELED_TRIP_TEST(2, -432.40),
    FULL_USE_CASH_ADVANCE_TEST(2, 0),
    PARTIAL_USE_CASH_ADVANCE_TEST(3, -20.00),
    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST(3, 15.00),
    MULTIPLE_CASH_ADVANCE_TEST(5, 40.00),
    ORPHANED_CASH_ADVANCE_TEST(5, 40.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST1(3, 60.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST2(3, 50.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST3(3, 65.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST4(4, 60.00),
    MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST(3, 75.00),
    PERSONAL_WITHOUT_CASH_TEST(3, 65.00),
    CASH_EXCEEDS_PERSONAL_TEST(4, 142.88),
    CASH_EQUALS_PERSONAL_TEST(6, 142.88),
    PERSONAL_EXCEEDS_CASH_TEST(6, 131.88),
    PERSONAL_AND_CASH_ADVANCE_TEST(6, 212.00),
    PERSONAL_CHARGE_AND_RETURN_TEST(6, 91.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST(6, 91.00),
    ATM_CASH_ADVANCE_TEST(2, 0),
    CASH_EXCEEDS_ATM_ADVANCE_TEST(3, 5.10),
    ATM_CASH_ADVANCE_WITH_FEE_TEST(4, 0),
    ATM_CASH_ADVANCE_UNUSED_TEST(3, -6.00),
    ATM_CASH_ADVANCE_FEE_AND_UNUSED_TEST(5, -6.00),
    MULTI_ATM_CASH_ADVANCE_TEST(4, 0),
    MULTI_REPORT_ATM_CASH_ADVANCE_TEST(4, 0),
    ATM_CASH_ADVANCE_MULTI_FEE_TEST(6, 0),
    ATM_AND_REQUESTED_CASH_ADVANCE_TEST(6, -13.00),
    PARSE_FLAT_FILE_NO_QUOTES_TEST("567", "02/01/2019", 3, 1230.36),
    PARSE_FLAT_FILE_WITH_QUOTES_TEST("568", "02/01/2019", 3, 1230.36),
    
    PDP_EXAMPLE(10, 0.00),
    PDP_TEST(4, 550);

    public final String batchId;
    public final String batchDate;
    public final Integer recordCount;
    public final double journalAmountTotal;

    private ConcurSAEFileFixture(int recordCount, double journalAmountTotal) {
        this(ConcurTestConstants.JAN_04_2017, recordCount, journalAmountTotal);
    }

    private ConcurSAEFileFixture(String batchDate, int recordCount, double journalAmountTotal) {
        this.batchId = Integer.toString(ordinal() + 1);
        this.batchDate = batchDate;
        this.recordCount = Integer.valueOf(recordCount);
        this.journalAmountTotal = journalAmountTotal;
    }

    private ConcurSAEFileFixture(String batchId, String batchDate, int recordCount, double journalAmountTotal) {
        this.batchId = batchId;
        this.batchDate = batchDate;
        this.recordCount = Integer.valueOf(recordCount);
        this.journalAmountTotal = journalAmountTotal;
    }

    public ConcurStandardAccountingExtractFile toExtractFile() {
        ConcurStandardAccountingExtractFile extractFile = toExtractFileWithoutDetailLines();
        
        List<ConcurSAEDetailLineFixture> lineFixtures = ConcurFixtureUtils.getFixturesContainingParentFixture(
                ConcurSAEDetailLineFixture.class, this, ConcurSAEDetailLineFixture::getExtractFile);
        
        List<ConcurStandardAccountingExtractDetailLine> detailLines = new ArrayList<>(lineFixtures.size());
        int nextSequenceNumber = 1;
        for (ConcurSAEDetailLineFixture lineFixture : lineFixtures) {
            ConcurStandardAccountingExtractDetailLine detailLine = lineFixture.toDetailLine();
            detailLine.setSequenceNumber(Integer.toString(nextSequenceNumber++));
            detailLines.add(detailLine);
        }
        extractFile.setConcurStandardAccountingExtractDetailLines(detailLines);
        
        return extractFile;
    }

    public ConcurStandardAccountingExtractFile toExtractFileWithoutDetailLines() {
        ConcurStandardAccountingExtractFile extractFile = new ConcurStandardAccountingExtractFile();
        extractFile.setOriginalFileName(name() + GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION);
        extractFile.setBatchId(batchId);
        extractFile.setBatchDate(ConcurFixtureUtils.toSqlDate(batchDate));
        extractFile.setRecordCount(recordCount);
        extractFile.setJournalAmountTotal(new KualiDecimal(journalAmountTotal));
        return extractFile;
    }

}
