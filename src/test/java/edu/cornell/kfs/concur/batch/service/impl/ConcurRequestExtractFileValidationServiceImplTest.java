package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public class ConcurRequestExtractFileValidationServiceImplTest {
    private ConcurRequestExtractFileValidationServiceImpl concurRequestExtractFileValidationService;
    
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurRequestExtractFileValidationService = new ConcurRequestExtractFileValidationServiceImpl();
    }
    
    @After
    public void tearDown() throws Exception {
        concurRequestExtractFileValidationService = null;
    }
    
    @Test
    public void testHeaderAmountMatches() {
        ConcurRequestExtractFile testFile = buildConcurRequestExtractFile();
        if (concurRequestExtractFileValidationService.fileApprovedAmountsMatchHeaderApprovedAmount(testFile)) {
            assertTrue("Valid: Header amount matched sum of row amounts from file.", true);
        }
        else {
            assertTrue("Invalid: Header amount did NOT match sum of row amounts from file.", false);
        } 
    }
    
    @Test
    public void testHeaderAmountDoesNotMatch() {
        ConcurRequestExtractFile testFile = buildConcurRequestExtractFile();
        testFile.setTotalApprovedAmount(new KualiDecimal(9.87));
        if (concurRequestExtractFileValidationService.fileApprovedAmountsMatchHeaderApprovedAmount(testFile)) {
            assertTrue("Invalid: Header amount matched row sum. They should NOT have matched.", false);
        }
        else {
            assertTrue("Valid: Header amount did NOT match sum of row amounts from file.", true);
        } 
    }
    
    @Test
    public void testHeaderRowCountMatches() {
        ConcurRequestExtractFile testFile = buildConcurRequestExtractFile();
        if (concurRequestExtractFileValidationService.fileRowCountMatchesHeaderRowCount(testFile)) {
            assertTrue("Valid: Header row count file row count.", true);
        }
        else {
            assertTrue("Invalid: Header should NOT have matched file row count", false);
        } 
    }
    
    @Test
    public void testHeaderRowCountDoesNotMatch() {
        ConcurRequestExtractFile testFile = buildConcurRequestExtractFile();
        testFile.setRecordCount(new Integer(3));
        if (concurRequestExtractFileValidationService.fileRowCountMatchesHeaderRowCount(testFile)) {
            assertTrue("Invalid: Header row count matched file row count. They should NOT have matched.", false);
        }
        else {
            assertTrue("Valid: Header row count did NOT match file row count.", true);
        } 
    }
    
    protected ConcurRequestExtractFile buildConcurRequestExtractFile() {
        ConcurRequestExtractFile testFile = new ConcurRequestExtractFile();
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        testFile.setBatchDate(today);
        testFile.setRecordCount(13);
        testFile.setTotalApprovedAmount(new KualiDecimal(5253.33));
        testFile.getRequestDetails().add(buildConcurRequestExtractRequestDetailFileLine(today, new String("34YA"), new KualiDecimal(10.00), 1));
        testFile.getRequestDetails().add(buildConcurRequestExtractRequestDetailFileLine(today, new String("34YE"), new KualiDecimal(40.00), 2));
        testFile.getRequestDetails().add(buildConcurRequestExtractRequestDetailFileLine(today, new String("34YH"), new KualiDecimal(200.00), 1));
        testFile.getRequestDetails().add(buildConcurRequestExtractRequestDetailFileLine(today, new String("34YK"), new KualiDecimal(3.33), 3));
        testFile.getRequestDetails().add(buildConcurRequestExtractRequestDetailFileLine(today, new String("34YN"), new KualiDecimal(5000.00), 1));
        return testFile;
    }
    
    protected ConcurRequestExtractRequestDetailFileLine buildConcurRequestExtractRequestDetailFileLine(Date batchDate, String requestId, KualiDecimal requestAmount, int numberOfEntryDetails) {
        ConcurRequestExtractRequestDetailFileLine detailLine = new ConcurRequestExtractRequestDetailFileLine();
        detailLine.setBatchDate(batchDate);
        detailLine.setRequestId(requestId);
        detailLine.setRequestAmount(requestAmount);
        for (int i=1; i<=numberOfEntryDetails; i++) {
            detailLine.getRequestEntryDetails().add(buildConcurRequestExtractRequestEntryDetailFileLine(new Integer(numberOfEntryDetails), requestId));
        }
        return detailLine;
    }
    
    
    protected ConcurRequestExtractRequestEntryDetailFileLine buildConcurRequestExtractRequestEntryDetailFileLine(Integer sequenceNumber, String requestId) {
        ConcurRequestExtractRequestEntryDetailFileLine entryDetailLine = new ConcurRequestExtractRequestEntryDetailFileLine();
        entryDetailLine.setSequenceNumber(sequenceNumber);
        entryDetailLine.setRequestId(requestId);
        return entryDetailLine;
    }
}
