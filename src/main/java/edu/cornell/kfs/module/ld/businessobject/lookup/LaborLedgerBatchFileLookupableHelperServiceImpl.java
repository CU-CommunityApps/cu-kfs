package edu.cornell.kfs.module.ld.businessobject.lookup;

import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupSearchServiceImpl;

import edu.cornell.kfs.sys.CUKFSConstants;

public class LaborLedgerBatchFileLookupableHelperServiceImpl extends BatchFileLookupSearchServiceImpl {

//    /**
//     * Override method so that it only returns the /staging/ld/enterpriseFeed directory as selected.
//     * 
//     * @see org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl#getSelectedPaths()
//     */
//    @Override
//    protected String[] getSelectedPaths() {
//        String[] selectedPaths = new String[1];
//        String path = CUKFSConstants.STAGING_DIR + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.LD_DIR
//                + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.ENTERPRISE_FEED_DIR;
//        selectedPaths[0] = path;
//        return selectedPaths;
//    }
}