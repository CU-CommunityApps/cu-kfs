package edu.cornell.kfs.module.ld.businessobject.lookup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl;

import edu.cornell.kfs.module.ld.businessobject.LaborLedgerBatchFile;
import edu.cornell.kfs.sys.CUKFSConstants;

public class LaborLedgerBatchFileLookupableHelperServiceImpl extends BatchFileLookupableHelperServiceImpl {
	
	
	@Override
	public List<LaborLedgerBatchFile> getSearchResults(Map<String, String> fieldValues) {
		List results = super.getSearchResults(fieldValues);
		List laborLedgerFiles = new ArrayList<LaborLedgerBatchFile>();
		for(Object file:  results ) {
			BatchFile batchFile= (BatchFile) file;
			LaborLedgerBatchFile laborLedgerBatchFile;
			try {
				laborLedgerBatchFile = new LaborLedgerBatchFile(batchFile.getId());
				laborLedgerFiles.add(laborLedgerBatchFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return laborLedgerFiles;
	}

    /**
     * Override method so that it only returns the /staging/ld/enterpriseFeed directory as selected.
     * 
     * @see org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl#getSelectedPaths()
     */
    @Override
    protected String[] getSelectedPaths() {
        String[] selectedPaths = new String[1];
        String path = CUKFSConstants.STAGING_DIR + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.LD_DIR
                + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + CUKFSConstants.ENTERPRISE_FEED_DIR;
        selectedPaths[0] = path;
        return selectedPaths;
    }
}