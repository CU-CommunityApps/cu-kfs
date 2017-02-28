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
    public void validateDetailCountIncorrectMatch() {
        ConcurStandardAccountingExtractFile file = buildConcurStandardAccountingExtractFile();
        file.setRecordCount(new Integer(5));
        try {
            concurStandardAccountingExtractService.validateDetailCount(file);
            assertTrue("The counts should not be equal", false);
        } catch (ValidationException ve) {
            assertTrue("We successfully validated counts.", true);
        }
    }
    
    protected ConcurStandardAccountingExtractFile buildConcurStandardAccountingExtractFile() {
        ConcurStandardAccountingExtractFile file = new ConcurStandardAccountingExtractFile();
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        file.setBatchDate(today);
        file.setRecordCount(new Integer(3));
        file.setJournalAmountTotal(new KualiDecimal(100));
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                ConcurConstants.ConcurPdpConstants.DEBIT, new KualiDecimal(100)));
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                ConcurConstants.ConcurPdpConstants.DEBIT, new KualiDecimal(50)));
        file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                ConcurConstants.ConcurPdpConstants.CREDIT, new KualiDecimal(50)));
        return file;
    }
    
    protected ConcurStandardAccountingExtractDetailLine buildConcurStandardAccountingExtractDetailLine(String debitCredit, KualiDecimal amount) {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setJounalDebitCredit(debitCredit);
        line.setJournalAmount(amount);
        return line;
    }

}
