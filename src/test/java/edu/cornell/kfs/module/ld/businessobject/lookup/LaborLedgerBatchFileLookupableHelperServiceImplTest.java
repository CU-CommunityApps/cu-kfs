package edu.cornell.kfs.module.ld.businessobject.lookup;

import edu.cornell.kfs.sys.CUKFSConstants;
import junit.framework.TestCase;

public class LaborLedgerBatchFileLookupableHelperServiceImplTest extends TestCase {

    private LaborLedgerBatchFileLookupableHelperServiceImpl laborLedgerBatchFileLookupableHelperService;
    
    public static final String LD_ENTERPRISE_FEED_PATH = CUKFSConstants.STAGING_DIR + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.LD_DIR
            + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.ENTERPRISE_FEED_DIR;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        laborLedgerBatchFileLookupableHelperService = new LaborLedgerBatchFileLookupableHelperServiceImpl();
    }
    
//	public void testFeed() {
//		assertEquals(LD_ENTERPRISE_FEED_PATH, laborLedgerBatchFileLookupableHelperService.getPathsToSearch(null));
//	}

}
