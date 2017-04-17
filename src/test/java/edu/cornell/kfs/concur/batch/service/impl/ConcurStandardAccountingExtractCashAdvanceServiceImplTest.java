package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;

public class ConcurStandardAccountingExtractCashAdvanceServiceImplTest {
    
    private ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;

    @Before
    public void setUp() throws Exception {
        concurStandardAccountingExtractCashAdvanceService = new ConcurStandardAccountingExtractCashAdvanceServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractCashAdvanceService = null;
    }

    @Test
    public void validateCashLine() {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setCashAdvanceCaKey("1242");
        assertTrue("Should be a cash advance line", concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(line));
    }
    
    @Test
    public void validateNonCashLine() {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        assertFalse("Should NOT be a cash advance line", concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(line));
    }

}
