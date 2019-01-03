package edu.cornell.kfs.sys.businessobject.lookup;

import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupSearchServiceImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CreateDoneBatchFileLookupableHelperServiceImpl extends BatchFileLookupSearchServiceImpl {
	
	protected CreateDoneBatchFileAuthorizationService createDoneAuthorizationService; 

	 @Override
	    public List<Map<String, Object>> getActionLinks(BusinessObjectBase businessObject, Person user) {
	        BatchFile batchFile = (BatchFile)businessObject;
	        List<Map<String, Object>> actionLinks = new LinkedList<>();

	        if (canCreateDoneFile(batchFile, user)) {
	            Map<String, Object> createDoneLink = new LinkedHashMap<>();
	            createDoneLink.put("label", "Create Done");
	            createDoneLink.put("url", getCreateDoneUrl(batchFile));
	            createDoneLink.put("method", "GET");
	            actionLinks.add(createDoneLink);
	        }

	        return actionLinks;
	    }

    protected boolean canCreateDoneFile(BatchFile batchFile, Person user) {
    	boolean isDoneFile = StringUtils.endsWith(batchFile.getFileName(), ".done");
    	return (!isDoneFile && createDoneAuthorizationService.canCreateDoneFile(batchFile, user));
    }
    
    protected HtmlData getCreateDoneUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "createDone");
        String href = UrlFactory.parameterizeUrl("../createDoneBatchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "createDone", "Create Done");
    }
    
    public CreateDoneBatchFileAuthorizationService getCreateDoneAuthorizationService() {
		return createDoneAuthorizationService;
	}

	public void setCreateDoneAuthorizationService(
			CreateDoneBatchFileAuthorizationService createDoneAuthorizationService) {
		this.createDoneAuthorizationService = createDoneAuthorizationService;
	}

}
