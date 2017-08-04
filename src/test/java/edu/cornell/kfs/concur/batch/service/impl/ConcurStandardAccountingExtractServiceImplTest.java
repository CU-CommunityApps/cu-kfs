package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.fixture.ConcurSAEDetailLineFixture;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;

public class ConcurStandardAccountingExtractServiceImplTest {
    
    ConcurStandardAccountingExtractServiceImpl concurStandardAccountingExtractServiceImpl;

    @Before
    public void setUp() throws Exception {
        ConcurBatchUtilityService concurBatchUtilityService = new ConcurBatchUtilityServiceImpl();
        concurStandardAccountingExtractServiceImpl = new ConcurStandardAccountingExtractServiceImpl();
        concurStandardAccountingExtractServiceImpl.setConcurBatchUtilityService(concurBatchUtilityService);
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractServiceImpl = null;
    }
    
    @Test
    public void buildPdpOutputFileName() {
        String originalFileName = "extract_CES_SAE_v3_20170316093817.txt";
        String expectedResults = "pdp_concur_extract_CES_SAE_v3_20170316093817.xml";
        assertEquals("The file name should be the orinal file name with a XML as the extension", expectedResults, 
                concurStandardAccountingExtractServiceImpl.buildPdpOutputFileName(originalFileName));
    }

    @Test
    public void testPDPInclusionCheckForSAELines() throws Exception {
        assertCorrectResultForPDPInclusionCheck(true, ConcurSAEDetailLineFixture.PDP_EXAMPLE_DEBIT);
        assertCorrectResultForPDPInclusionCheck(true, ConcurSAEDetailLineFixture.PDP_EXAMPLE_CASH_ADVANCE);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_PRE_PAID_AMOUNT);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_UNUSED_CASH_ADVANCE_AMOUNT);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_CORP_CARD_DEBIT);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_CANCELED_TRIP_CORP_CARD_CREDIT);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_PERSONAL_DEBIT);
        assertCorrectResultForPDPInclusionCheck(true, ConcurSAEDetailLineFixture.PDP_EXAMPLE_PERSONAL_CREDIT);
        assertCorrectResultForPDPInclusionCheck(true, ConcurSAEDetailLineFixture.PDP_EXAMPLE_PERSONAL_RETURN_DEBIT);
        assertCorrectResultForPDPInclusionCheck(false, ConcurSAEDetailLineFixture.PDP_EXAMPLE_PERSONAL_RETURN_CREDIT);
    }

    protected void assertCorrectResultForPDPInclusionCheck(boolean expectedResult, ConcurSAEDetailLineFixture fixture) throws Exception {
        ConcurStandardAccountingExtractDetailLine line = fixture.toDetailLine();
        boolean actualResult = concurStandardAccountingExtractServiceImpl.shouldProcessSAELineToPDP(line);
        assertEquals("Wrong result when checking whether SAE line should be included in PDP output", expectedResult, actualResult);
    }

}
