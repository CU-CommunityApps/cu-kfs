package edu.cornell.kfs.module.ld.businessobject;

import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl;

import edu.cornell.kfs.sys.CUKFSConstants;

public class LaborLedgerBatchFileLookupableHelperServiceImpl extends BatchFileLookupableHelperServiceImpl {

    /**
     * Override method so that it only returns the /staging/ld/enterpriseFeed directory as selected.
     * 
     * @see org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl#getSelectedPaths()
     */
    @Override
    protected String[] getSelectedPaths() {
        String[] selectedPaths = new String[1];
        String path = CUKFSConstants.STAGING_DIR + System.getProperty("file.separator") + CUKFSConstants.LD_DIR + System.getProperty("file.separator") + CUKFSConstants.ENTERPRISE_FEED_DIR;
        selectedPaths[0] = path;
        return selectedPaths;
    }
}