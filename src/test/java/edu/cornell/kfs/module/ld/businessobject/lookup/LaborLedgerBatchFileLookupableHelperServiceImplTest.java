package edu.cornell.kfs.module.ld.businessobject.lookup;

import junit.framework.TestCase;

public class LaborLedgerBatchFileLookupableHelperServiceImplTest extends TestCase {

    private LaborLedgerBatchFileLookupableHelperServiceImpl laborLedgerBatchFileLookupableHelperService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        laborLedgerBatchFileLookupableHelperService = new LaborLedgerBatchFileLookupableHelperServiceImpl();
    }
    
    public void testFeed()   {
    	assertEquals("staging/ld/enterpriseFeed",laborLedgerBatchFileLookupableHelperService.getSelectedPaths()[0]);    
    }

}
