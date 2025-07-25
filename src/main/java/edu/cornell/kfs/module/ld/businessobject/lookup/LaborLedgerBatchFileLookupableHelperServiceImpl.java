package edu.cornell.kfs.module.ld.businessobject.lookup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.businessobject.service.impl.BatchFileSearchService;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.module.ld.businessobject.LaborLedgerBatchFile;
import edu.cornell.kfs.sys.CUKFSConstants;

public class LaborLedgerBatchFileLookupableHelperServiceImpl extends BatchFileSearchService {
	private static final Logger LOG = LogManager.getLogger(LaborLedgerBatchFileLookupableHelperServiceImpl.class);

	@Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final MultiValueMap<String, String> fieldValues, final int skip, final int limit, final String sortField, final boolean sortAscending) {
        Pair<Collection<? extends BusinessObjectBase>, Integer>  results = super.getSearchResults(businessObjectClass, fieldValues, skip, limit, sortField, sortAscending);
        List<BusinessObjectBase> laborLedgerFiles = new ArrayList<BusinessObjectBase>();
        for (Object file : results.getLeft()) {
            BatchFile batchFile = (BatchFile) file;
            LaborLedgerBatchFile laborLedgerFile;
            try {
                laborLedgerFile = new LaborLedgerBatchFile(batchFile.getId());
                laborLedgerFiles.add(laborLedgerFile);
            } catch (FileNotFoundException e) {
                LOG.error(
                        "getSearchResults(): An error has occured while creating a LaborLedgerBatchFile from the BatchFile object in the search results: "
                                + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return Pair.of(laborLedgerFiles, laborLedgerFiles.size());
	}

	/**
	 * Override method so that it only returns the /staging/ld/enterpriseFeed
	 * directory as selected.
	 * 
	 */
	@Override
	protected List<String> getPathsToSearch(final List<String> selectedPaths) {
	    final List<String> searchPaths = new ArrayList<>();
		String path = CUKFSConstants.STAGING_DIR + System.getProperty(CUKFSConstants.FILE_SEPARATOR)
				+ CUKFSConstants.LD_DIR + System.getProperty(CUKFSConstants.FILE_SEPARATOR)
				+ CUKFSConstants.ENTERPRISE_FEED_DIR;
		searchPaths.add(path);
		return searchPaths;
	}
}