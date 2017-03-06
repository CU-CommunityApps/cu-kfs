package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.util.type.KualiDecimal;

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
        
        KualiDecimal[] debits = {new KualiDecimal(100.75), new KualiDecimal(-50.45)};
        KualiDecimal[] credits  = {};
        file = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits);
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractService = null;
        file = null;
    }
    
    @Test
    public void validateDetailCountGood() {
        assertTrue("The counts should be been the same.", concurStandardAccountingExtractService.validateDetailCount(file));
    }
    
    @Test
    public void validateDetailCountIncorrectMatch() {
        file.setRecordCount(new Integer(5));
        assertFalse("The counts should NOT be been the same.", concurStandardAccountingExtractService.validateDetailCount(file));
    }
    
    @Test
    public void validateAmountsGood() {
        assertTrue("The amounts should equal.", concurStandardAccountingExtractService.validateAmounts(file));
    }
    
    @Test
    public void validateAmountsAmountMismatch() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJournalAmount(new KualiDecimal(200));
        assertFalse("The amounts should NOT be equal.", concurStandardAccountingExtractService.validateAmounts(file));
    }
    
    @Test
    public void validateAmountsIncorrectDebitCredit() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJounalDebitCredit("foo");
        String message = "The should throw an error due to incorrect debitCredit field";
        assertFalse("The should throw an error due to incorrect debitCredit field", concurStandardAccountingExtractService.validateAmounts(file));
    }
    
    @Test
    public void valdiateDateGood() {
        Date testDate = new Date(Calendar.getInstance().getTimeInMillis());
        assertTrue("The date should be valid.", concurStandardAccountingExtractService.validateDate(testDate));
    }
    
    @Test
    public void valdiateDateBad() {
        Date testDate = null;
        String message = "The date should NOT be valid.";
        assertFalse("The date should NOT be valid.", concurStandardAccountingExtractService.validateDate(testDate));
    }

}
