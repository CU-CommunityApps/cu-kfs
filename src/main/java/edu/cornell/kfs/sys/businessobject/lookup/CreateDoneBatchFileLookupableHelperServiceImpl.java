package edu.cornell.kfs.sys.businessobject.lookup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupSearchServiceImpl;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.batch.CreateDoneBatchFile;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;

public class CreateDoneBatchFileLookupableHelperServiceImpl extends BatchFileLookupSearchServiceImpl {
	private static final Logger LOG = LogManager.getLogger(CreateDoneBatchFileLookupableHelperServiceImpl.class);

	protected CreateDoneBatchFileAuthorizationService createDoneAuthorizationService;

	@Override
	public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(Class<? extends BusinessObjectBase> businessObjectClass,
            MultivaluedMap<String, String> fieldValues, int skip, int limit, String sortField, boolean sortAscending) {

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

	@Override
	public List<Map<String, Object>> getActionLinks(BusinessObjectBase businessObject, Person user) {
		BatchFile batchFile = (BatchFile) businessObject;
		List<Map<String, Object>> actionLinks = new LinkedList<>();

		if (canCreateDoneFile(batchFile, user)) {
			Map<String, Object> createDoneLink = new LinkedHashMap<>();
			createDoneLink.put(CUKFSPropertyConstants.LOOKUP_RESULT_ACTION_LABEL, "Create Done");
			createDoneLink.put(CUKFSPropertyConstants.LOOKUP_RESULT_ACTION_URL,
					KRAD_URL_PREFIX + getCreateDoneUrl(batchFile));
			createDoneLink.put(CUKFSPropertyConstants.LOOKUP_RESULT_ACTION_METHOD, "GET");
			actionLinks.add(createDoneLink);
		}

		return actionLinks;
	}

	protected boolean canCreateDoneFile(BatchFile batchFile, Person user) {
		boolean isDoneFile = StringUtils.endsWith(batchFile.getFileName(), ".done");
		return (!isDoneFile && createDoneAuthorizationService.canCreateDoneFile(batchFile, user));
	}

	protected String getCreateDoneUrl(BatchFile batchFile) {
		Properties parameters = new Properties();
		parameters.put("filePath",
				BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
		parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "createDone");
		return UrlFactory.parameterizeUrl("../createDoneBatchFileAdmin.do", parameters);
	}

	public CreateDoneBatchFileAuthorizationService getCreateDoneAuthorizationService() {
		return createDoneAuthorizationService;
	}

	public void setCreateDoneAuthorizationService(
			CreateDoneBatchFileAuthorizationService createDoneAuthorizationService) {
		this.createDoneAuthorizationService = createDoneAuthorizationService;
	}

}
