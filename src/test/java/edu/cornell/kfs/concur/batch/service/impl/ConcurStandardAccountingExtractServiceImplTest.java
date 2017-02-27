package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public class ConcurStandardAccountingExtractServiceImplTest {
    
    ConcurStandardAccountingExtractServiceImpl concurStandardAccountingExtractService;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class).setLevel(Level.DEBUG);
        concurStandardAccountingExtractService = new ConcurStandardAccountingExtractServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractService = null;
    }

    @Test
    public void validateBatchDateGood() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        try {
            concurStandardAccountingExtractService.validateBatchDate(file.getBatchDate());
            assertTrue("We successfully converted the batch date.", true);
        } catch (ValidationException ve) {
            assertTrue("We should have been able to conver this date.", false);
        }
    }
    
    @Test
    public void validateBatchDateBadAlphaDate() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        file.setBatchDate("Foo");
        try {
            concurStandardAccountingExtractService.validateBatchDate(file.getBatchDate());
            assertTrue("Foo is not a date, should should have a validation error.", false);
        } catch (ValidationException ve) {
            assertTrue("Successfully had a validation exception on Foo", true);
        }
    }
    
    @Test
    public void validateBatchDateBadNumbericDate() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        file.setBatchDate("20170227");
        try {
            concurStandardAccountingExtractService.validateBatchDate(file.getBatchDate());
            assertTrue("20170227 is not formatted correctly, so it should have thrown a validation error.", false);
        } catch (ValidationException ve) {
            assertTrue("Successfully errored on 20170227", true);
        }
    }
    
    @Test
    public void validateDetailCountGood() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("We successfully validated counts.", true);
        } catch (ValidationException ve) {
            assertTrue("The counts should be been the same.", false);
        }
    }
    
    @Test
    public void validateDetailCountBad() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        file.setRecordCount("5");
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("The counts should not be equal", false);
        } catch (ValidationException ve) {
            assertTrue("We successfully validated counts.", true);
        }
    }
    @Test
    public void validateDetailCountInvalidNumberBad() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        file.setRecordCount("foo");
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("The counts should not be equal", false);
        } catch (ValidationException ve) {
            assertTrue("We successfully validated counts.", true);
        }
    }
    
    protected ConcurStandardAccountingExtractFile buildConcurStandardAccountingExtractFile() {
        ConcurStandardAccountingExtractFile file = new ConcurStandardAccountingExtractFile();
        file.setBatchDate("2017-02-27");
        file.setRecordCount("3");
        file.setJournalAmountTotal("100");
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine("DEBIT", "100"));
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine("DEBIT", "50"));
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine("CREDIT", "50"));
        return file;
    }
    
    protected ConcurStandardAccountingExtractDetailLine buildConcurStandardAccountingExtractDetailLine(String debitCredit, String amount) {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setJounalDebitCredit(debitCredit);
        line.setJournalAmount(amount);
        return line;
    }

}
