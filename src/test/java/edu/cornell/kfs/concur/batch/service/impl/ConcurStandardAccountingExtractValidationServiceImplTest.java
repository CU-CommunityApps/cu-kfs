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

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurStandardAccountingExtractFileFixture;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractValidationServiceImplTest {
    
    private ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractService;
    private ConcurStandardAccountingExtractFile file;
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurStandardAccountingExtractService = new ConcurStandardAccountingExtractValidationServiceImpl();

        double[] debits = {100.75, -50.45};
        double[] credits  = {50.45};
        file = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits);
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractService = null;
        file = null;
    }
    
    @Test
    public void validateDetailCountGood() {
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("We successfully validated counts.", true);
        } catch (ValidationException ve) {
            assertTrue("The counts should be been the same.", false);
        }
    }
    
    @Test
    public void validateDetailCountIncorrectMatch() {
        file.setRecordCount(new Integer(5));
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("The counts should not be equal", false);
        } catch (ValidationException ve) {
            assertTrue("We successfully validated counts.", true);
        }
    }
    
    @Test
    public void validateAmountsGood() {
        String message = "The amounts should equal";
        try {
            concurStandardAccountingExtractService.validateAmounts(file);
            assertTrue(message, true);
        } catch (ValidationException ve) {
            assertTrue(message, false);
        }
    }
    
    @Test
    public void validateAmountsAmountMismatch() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJournalAmount(new KualiDecimal(200));
        String message = "The amounts should NOT equal and throw a validation error";
        try {
            concurStandardAccountingExtractService.validateAmounts(file);
            assertTrue(message, false);
        } catch (ValidationException ve) {
            assertTrue(message, true);
        }
    }
    
    @Test
    public void validateAmountsIncorrectDebitCredit() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJounalDebitCredit("foo");
        String message = "The should throw an error due to incorrect debitCredit field";
        try {
            concurStandardAccountingExtractService.validateAmounts(file);
            assertTrue(message, false);
        } catch (ValidationException ve) {
            assertTrue(message, true);
        }
    }
    
    @Test
    public void valdiateDateGood() {
        Date testDate = new Date(Calendar.getInstance().getTimeInMillis());
        String message = "The date should be valid.";
        try {
            concurStandardAccountingExtractService.validateDate(testDate);
            assertTrue(message, true);
        } catch (ValidationException ve) {
            assertTrue(message, false);
        }
    }
    
    @Test
    public void valdiateDateBad() {
        Date testDate = null;
        String message = "The date should NOT be valid.";
        try {
            concurStandardAccountingExtractService.validateDate(testDate);
            assertTrue(message, false);
        } catch (ValidationException ve) {
            assertTrue(message, true);
        }
    }

}
