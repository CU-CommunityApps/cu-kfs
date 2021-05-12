package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.concur.batch.fixture.PdpFeedDetailEntryFixture;
import edu.cornell.kfs.concur.batch.fixture.PdpFeedFileBaseEntryFixture;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.impl.ConcurStandardAccountExtractPdpEntryServiceImpl.DebitCreditTotal;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;

public class ConcurStandardAccountExtractPdpEntryServiceImplTest {
    
    private ConcurStandardAccountExtractPdpEntryServiceImpl pdpEntryService;
    private ConcurStandardAccountingExtractBatchReportData reportData;

    @Before
    public void setUp() throws Exception {
        pdpEntryService = new ConcurStandardAccountExtractPdpEntryServiceImpl();
        pdpEntryService.setPayeeNameFieldSize(new Integer(40));
        pdpEntryService.setDateTimeService(new DateTimeServiceImpl());
        reportData = new ConcurStandardAccountingExtractBatchReportData();
    }

    @After
    public void tearDown() throws Exception {
        pdpEntryService = null;
        reportData = null;
    }

    @Test
    public void buildPayeeName() {
        String results = pdpEntryService.buildPayeeName("Doe", "John", "Q");
        String expected = "Doe, John Q.";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameNoMiddleInitial() {
        String results = pdpEntryService.buildPayeeName("Doe", "John", "   ");
        String expected = "Doe, John";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyChars() {
        String results = pdpEntryService.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghjklz", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghjk";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma() {
        String results = pdpEntryService.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghij", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghij";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma2() {
        String results = pdpEntryService.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghijk", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghij";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void formatDate1() {
        DateTime jodaDateTime = new DateTime(2017, 2, 7, 0, 0);
        Date testDate = new Date(jodaDateTime.getMillis());
        String results = pdpEntryService.formatDate(testDate);
        assertEquals("The dates should format as expected", "02/07/2017", results);
    }
    
    @Test
    public void formatDate2() {
        DateTime jodaDateTime = new DateTime(1977, 12, 31, 3, 46);
        Date testDate = new Date(jodaDateTime.getMillis());
        String results = pdpEntryService.formatDate(testDate);
        assertEquals("The dates should format as expected", "12/31/1977", results);
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_NoGroups() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_ONE_TRANS_ZERO.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group", 1, pdpFeedFileBaseEntry.getGroup().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have no groups", 0, cleaned.getGroup().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_OneTransaction() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_ONE_TRANS_ZER_ONE_TRANS_POSITIVE.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group", 1, pdpFeedFileBaseEntry.getGroup().size());
        assertEquals("Should have 2 transactions", 2, pdpFeedFileBaseEntry.getGroup().get(0).getDetail().get(0).getAccounting().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have 1 group", 1, cleaned.getGroup().size());
        assertEquals("Should have 1 detail", 1, cleaned.getGroup().get(0).getDetail().size());
        assertEquals("Should have 1 transaction", 1, cleaned.getGroup().get(0).getDetail().get(0).getAccounting().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_TwoTransSumPositive() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_TWO_TRANS_SUM_TO_POSITIVE.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group", 1, pdpFeedFileBaseEntry.getGroup().size());
        assertEquals("Should have 2 transactions", 2, pdpFeedFileBaseEntry.getGroup().get(0).getDetail().get(0).getAccounting().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have 1 group", 1, cleaned.getGroup().size());
        assertEquals("Should have 1 detail", 1, cleaned.getGroup().get(0).getDetail().size());
        assertEquals("Should have 1 transaction", 1, cleaned.getGroup().get(0).getDetail().get(0).getAccounting().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_TransSumToZero() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_TWO_TRANS_SUM_TO_ZERO.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group", 1, pdpFeedFileBaseEntry.getGroup().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have no groups", 0, cleaned.getGroup().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_TransSumToNegative() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_TWO_TRANS_SUM_TO_NEGATIVE.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group", 1, pdpFeedFileBaseEntry.getGroup().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have no groups", 0, cleaned.getGroup().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_MultipleDetailsZeroTrans() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_TWO_DETAILS_ONE_POSITIVE_ONE_ZERO.toPdpFeedFileBaseEntry();
        assertEquals("Should have 1 group in original feed file", 1, pdpFeedFileBaseEntry.getGroup().size());
        assertEquals("Should have 2 details in original feed file", 2, pdpFeedFileBaseEntry.getGroup().get(0).getDetail().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have 1 group in cleaned feed file", 1, cleaned.getGroup().size());
        assertEquals("Should have 1 detail in cleaned feedfile", 1, cleaned.getGroup().get(0).getDetail().size());
    }
    
    @Test
    public void createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections_MultipleGroups() {
        PdpFeedFileBaseEntry pdpFeedFileBaseEntry = PdpFeedFileBaseEntryFixture.FEED_TWO_GROUPS_ONE_ZERO.toPdpFeedFileBaseEntry();
        assertEquals("Should have 2 group in original feed file", 2, pdpFeedFileBaseEntry.getGroup().size());
        PdpFeedFileBaseEntry cleaned = pdpEntryService.createPdpFileBaseEntryThatDoesNotContainNonReimbursableSections(pdpFeedFileBaseEntry, reportData);
        assertEquals("Should have 1 group in cleaned feed file", 1, cleaned.getGroup().size());
    }
    
    @Test
    public void cleanAccountingEntriesInDetailEntry_HandleOneCredit() {
        PdpFeedDetailEntry detailEntry = PdpFeedDetailEntryFixture.DETAIL_TWO_POSITIVE_1_NEGATIVE.toPdpFeedDetailEntry();
        Map<DebitCreditTotal, KualiDecimal> totals = pdpEntryService.calculateTotals(detailEntry);
        assertEquals("The number of accounting entries should be 3", 3, detailEntry.getAccounting().size());
        assertTotals(totals, 15, -10);
        
        pdpEntryService.cleanAccountingEntriesInDetailEntry(detailEntry);
        
        totals = pdpEntryService.calculateTotals(detailEntry);
        assertEquals("The number of accounting entries should be 1", 1, detailEntry.getAccounting().size());
        assertTotals(totals, 5, 0);
    }
    
    @Test
    public void cleanAccountingEntriesInDetailEntry_HandleTwoCredits() {
        PdpFeedDetailEntry detailEntry = PdpFeedDetailEntryFixture.DETAIL_TWO_POSITIVE_2_NEGATIVE.toPdpFeedDetailEntry();
        Map<DebitCreditTotal, KualiDecimal> totals = pdpEntryService.calculateTotals(detailEntry);
        assertEquals("The number of accounting entries should be 4", 4, detailEntry.getAccounting().size());
        assertTotals(totals, 15, -11);
        
        pdpEntryService.cleanAccountingEntriesInDetailEntry(detailEntry);
        
        totals = pdpEntryService.calculateTotals(detailEntry);
        assertEquals("The number of accounting entries should be 1", 1, detailEntry.getAccounting().size());
        assertTotals(totals, 4, 0);
    }
    
    private void assertTotals(Map<DebitCreditTotal, KualiDecimal> totals, double debitTotal, double creditTotal) {
        assertEquals("Debit total expected to be " + debitTotal, new KualiDecimal(debitTotal), totals.get(DebitCreditTotal.DEBIT));
        assertEquals("Credit total expected to be " + creditTotal, new KualiDecimal(creditTotal), totals.get(DebitCreditTotal.CREDIT));
    }

}
