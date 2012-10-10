/**
 * 
 */
package edu.cornell.kfs.sys.web.struts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.module.purap.util.ElectronicInvoiceUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.web.struts.action.KualiAction;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.service.DocumentRequeueFileBuilderService;

/**
 * Struts Action for printing Purap documents outside of a document action
 */
public class DocumentRequeueFileBuilderAction extends KualiAction {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRequeueFileBuilderAction.class);
    
    private static final String ACTION = "action";
    private static final String GENERATE = "generate";
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	super.execute(mapping, form, request, response);

        String action = request.getParameter(ACTION);
        
        if (GENERATE.equalsIgnoreCase(action)) {
	        LOG.info("Generating document requeuer file. ");
	        
	        int recordCount = SpringContext.getBean(DocumentRequeueFileBuilderService.class).generateRequeueFile();
	        
	    	// Output the resulting count from the file build job
	    	MessageMap messageMap = GlobalVariables.getMessageMap();
	    	messageMap.putInfoWithoutFullErrorPath("Document", CUKFSKeyConstants.MESSAGE_DOCUMENT_REQUEUER_RESULTS, Integer.toString(recordCount));
	        LOG.info("Document requeuer file generation complete. ");
        }   	
    	return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
}