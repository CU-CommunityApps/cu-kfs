package edu.cornell.kfs.sys.web.struts;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminAction;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminForm;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.batch.CreateDoneBatchFile;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;

public class CreateDoneKualiBatchFileAdminAction extends KualiBatchFileAdminAction {
	
    public ActionForward createDone(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiBatchFileAdminForm fileAdminForm = (KualiBatchFileAdminForm) form;
        String filePath = BatchFileUtils.resolvePathToAbsolutePath(fileAdminForm.getFilePath());
        File file = new File(filePath).getAbsoluteFile();
        BatchFile batchFile = new BatchFile(file);

        if (!SpringContext.getBean(CreateDoneBatchFileAuthorizationService.class).canCreateDoneFile(batchFile, GlobalVariables.getUserSession().getPerson())) {
            throw new RuntimeException("Error: not authorized to create a .done file");
        }
        
        String status = createDoneFile(filePath);
        request.setAttribute("status", status);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * Creates a '.done' file with the name of the original file.
     */
    protected String createDoneFile(String filename) {
    	String status = null;
        String doneFileName =  StringUtils.substringBeforeLast(filename,".") + ".done";
        File doneFile = new File(doneFileName);
        
        ConfigurationService kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);

        if (!doneFile.exists()) {
            boolean doneFileCreated = false;
            try {
                doneFileCreated = doneFile.createNewFile();
                status = kualiConfigurationService.getPropertyValueAsString( CUKFSKeyConstants.MESSAGE_DONE_FILE_SUCCESSFULLY_CREATED);
            }
            catch (IOException e) {
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " + doneFileName, e);
            }

            if (!doneFileCreated) {
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " + doneFileName);
            }
        }
        else{
        	status = kualiConfigurationService.getPropertyValueAsString( CUKFSKeyConstants.MESSAGE_DONE_FILE_ALREADY_EXISTS);
        }
        
        return status;
    }

}
