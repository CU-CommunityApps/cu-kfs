package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEDetailLineFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEFileFixture;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

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
    public void validateIsCashAdvanceTrue() {
        ConcurStandardAccountingExtractDetailLine line = ConcurSAEDetailLineFixture.PDP_TEST_CASH_ADVANCE_500.toDetailLine();
        line.setCashAdvanceKey("1242");
        assertTrue("Should be a cash advance line", concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(line));
    }
    
    @Test
    public void validateIsCashAdvanceFalse() {
        ConcurStandardAccountingExtractDetailLine line = ConcurSAEDetailLineFixture.PDP_TEST_DEBIT_1_50.toDetailLine();
        assertFalse("Should NOT be a cash advance line", concurStandardAccountingExtractCashAdvanceService.isCashAdvanceLine(line));
    }
    
    @Test
    public void validateFindAccountingInfoForCashAdvanceLine() {
        List<ConcurStandardAccountingExtractDetailLine> saeLines = buildSAELines();
        ConcurStandardAccountingExtractDetailLine line = ConcurSAEDetailLineFixture.PDP_TEST_CASH_ADVANCE_500.toDetailLine();
        ConcurAccountInfo info = concurStandardAccountingExtractCashAdvanceService.findAccountingInfoForCashAdvanceLine(line, saeLines);
        assertEquals("Charts should match", ParameterTestValues.COLLECTOR_CHART_CODE, info.getChart());
        assertEquals("Accounts should match", ConcurTestConstants.ACCT_1234321, info.getAccountNumber());
    }

    private List<ConcurStandardAccountingExtractDetailLine> buildSAELines() {
        List<ConcurSAEDetailLineFixture> lineFixtures = ConcurFixtureUtils.getFixturesContainingParentFixture(
                ConcurSAEDetailLineFixture.class, ConcurSAEFileFixture.PDP_TEST, ConcurSAEDetailLineFixture::getExtractFile);
        List<ConcurStandardAccountingExtractDetailLine> saeLines = new ArrayList<ConcurStandardAccountingExtractDetailLine>();
        for (ConcurSAEDetailLineFixture fixture : lineFixtures) {
            saeLines.add(fixture.toDetailLine());
        }
        return saeLines;
    }

}
