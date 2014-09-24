package edu.cornell.kfs.module.ld.businessobject.lookup;

import org.kuali.kfs.sys.context.KualiTestBase;

public class LaborLedgerBatchFileLookupableHelperServiceImplTest extends KualiTestBase {

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
