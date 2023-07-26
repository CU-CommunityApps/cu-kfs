package edu.cornell.kfs.sys.businessobject.lookup;

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

import edu.cornell.kfs.sys.batch.CreateDoneBatchFile;

public class CreateDoneBatchFileLookupableHelperServiceImpl extends BatchFileSearchService {
	private static final Logger LOG = LogManager.getLogger(CreateDoneBatchFileLookupableHelperServiceImpl.class);

	public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final MultiValueMap<String, String> fieldValues, final int skip, final int limit, final String sortField, final boolean sortAscending) {

		Pair<Collection<? extends BusinessObjectBase>, Integer>  results = super.getSearchResults(businessObjectClass, fieldValues, skip, limit, sortField, sortAscending);
		List<BusinessObjectBase> createDoneBatchFiles = new ArrayList<BusinessObjectBase>();
		for (Object file : results.getLeft()) {
			BatchFile batchFile = (BatchFile) file;
			CreateDoneBatchFile createDoneBatchFile;
			try {
				createDoneBatchFile = new CreateDoneBatchFile(batchFile.getId());
				createDoneBatchFiles.add(createDoneBatchFile);
			} catch (FileNotFoundException e) {
				LOG.error(
						"getSearchResults(): An error has occured while creating a CreateDoneBatchFile from the BatchFile object in the search results: "
								+ e.getMessage());
				throw new RuntimeException(e);
			}
		}
		return Pair.of(createDoneBatchFiles, createDoneBatchFiles.size());
	}

}
