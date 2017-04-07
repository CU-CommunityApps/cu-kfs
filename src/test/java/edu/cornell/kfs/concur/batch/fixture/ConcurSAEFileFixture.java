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
    CANCELED_TRIP_TEST(2, -432.40);

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
