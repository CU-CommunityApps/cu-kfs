package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurStandardAccountingExtractServiceImplTest {
    
    ConcurStandardAccountingExtractServiceImpl concurStandardAccountingExtractServiceImpl;

    @Before
    public void setUp() throws Exception {
        concurStandardAccountingExtractServiceImpl = new ConcurStandardAccountingExtractServiceImpl();
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

}
