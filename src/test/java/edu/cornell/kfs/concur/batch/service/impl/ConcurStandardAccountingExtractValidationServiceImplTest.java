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
    
    private ConcurStandardAccountingExtractValidationService concurStandardAccountingValidationService;
    private ConcurStandardAccountingExtractFile file;
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurStandardAccountingValidationService = new ConcurStandardAccountingExtractValidationServiceImpl();
        
        KualiDecimal[] debits = {new KualiDecimal(100.75), new KualiDecimal(-50.45)};
        KualiDecimal[] credits  = {};
        file = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits);
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingValidationService = null;
        file = null;
    }
    
    @Test
    public void validateDetailCountGood() {
        assertTrue("The counts should be been the same.", concurStandardAccountingValidationService.validateDetailCount(file));
    }
    
    @Test
    public void validateDetailCountIncorrectMatch() {
        setBadRecordCount();
        assertFalse("The counts should NOT be been the same.", concurStandardAccountingValidationService.validateDetailCount(file));
    }
    
    @Test
    public void validateAmountsGood() {
        assertTrue("The amounts should equal.", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(file));
    }
    
    @Test
    public void validateAmountsAmountMismatch() {
        setBadJournalTotal();
        assertFalse("The amounts should NOT be equal.", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(file));
    }
    
    @Test
    public void validateAmountsIncorrectDebitCredit() {
        setBadDebitCredit();
        assertFalse("The should throw an error due to incorrect debitCredit field", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(file));
    }
    
    @Test
    public void valdiateDateGood() {
        Date testDate = new Date(Calendar.getInstance().getTimeInMillis());
        assertTrue("The date should be valid.", concurStandardAccountingValidationService.validateDate(testDate));
    }
    
    @Test
    public void valdiateDateBad() {
        Date testDate = null;
        assertFalse("The date should NOT be valid.", concurStandardAccountingValidationService.validateDate(testDate));
    }
    
    @Test
    public void validateGeneralValidationGood() {
        assertTrue("General Validation should be good.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(file));
    }
    
    @Test
    public void validateGeneralValidationBadDate() {
        file.setBatchDate(null);
        assertFalse("General validation should be false, bad date.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(file));
    }
    
    @Test
    public void validateGeneralValidationBadAmount() {
        setBadJournalTotal();
        assertFalse("General validation should be false, bad journal total.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(file));
    }
    
    @Test
    public void validateGeneralValidationBadDebitCredit() {
        setBadDebitCredit();
        assertFalse("General validation should be false, bad debit credit field.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(file));
    }
    
    @Test
    public void validateGeneralValidationBadCount() {
        setBadRecordCount();
        assertFalse("General validation should be false, bad line count.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(file));
    }
    
    private void setBadJournalTotal() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJournalAmount(new KualiDecimal(200));
    }
    
    private void setBadDebitCredit() {
        file.getConcurStandardAccountingExtractDetailLines().get(0).setJounalDebitCredit("foo");
    }

    private void setBadRecordCount() {
        file.setRecordCount(new Integer(5));
    }

}
