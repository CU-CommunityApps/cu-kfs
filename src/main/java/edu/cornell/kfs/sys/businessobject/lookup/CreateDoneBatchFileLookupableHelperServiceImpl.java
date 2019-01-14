package edu.cornell.kfs.sys.businessobject.lookup;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupSearchServiceImpl;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;

public class CreateDoneBatchFileLookupableHelperServiceImpl extends BatchFileLookupSearchServiceImpl {

	protected CreateDoneBatchFileAuthorizationService createDoneAuthorizationService;

	@Override
	public List<Map<String, Object>> getActionLinks(BusinessObjectBase businessObject, Person user) {
		BatchFile batchFile = (BatchFile) businessObject;
		List<Map<String, Object>> actionLinks = new LinkedList<>();

		if (canCreateDoneFile(batchFile, user)) {
			Map<String, Object> createDoneLink = new LinkedHashMap<>();
			createDoneLink.put(CUKFSPropertyConstants.LOOKUP_RESULT_ACTION_LABEL, "Create Done");
			createDoneLink.put(CUKFSPropertyConstants.LOOKUP_RESULT_ACTION_URL, KRAD_URL_PREFIX + getCreateDoneUrl(batchFile));
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
