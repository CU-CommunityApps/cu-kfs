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
        String originalFileName = "extract_CES_SAE_v3_p0025644mo4c_20170227063221.txt";
        String expectedResults = "extract_CES_SAE_v3_p0025644mo4c_20170227063221.xml";
        assertEquals("The file name should be the orinal file name with a XML as the extension", expectedResults, 
                concurStandardAccountingExtractServiceImpl.buildPdpOutputFileName(originalFileName));
    }

}
