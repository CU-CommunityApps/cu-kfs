package edu.cornell.kfs.module.bc.document.web.struts;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.BCKeyConstants;
import org.kuali.kfs.module.bc.CUBCKeyConstants;
import org.kuali.kfs.module.bc.document.service.PayrateExportService;
import org.kuali.kfs.module.bc.document.service.PayrateImportService;
import org.kuali.kfs.module.bc.document.web.struts.BudgetExpansionAction;
import org.kuali.kfs.module.bc.service.HumanResourcesPayrollService;
import org.kuali.kfs.module.bc.util.ExternalizedMessageWrapper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.WebUtils;

import edu.cornell.kfs.module.bc.document.service.SipImportService;

public class SipImportAction extends BudgetExpansionAction {
    
    public ActionForward performImport(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	SipImportForm sipImportForm = (SipImportForm) form;
        SipImportService sipImportService = SpringContext.getBean(SipImportService.class);
        List<ExternalizedMessageWrapper> messageList = new ArrayList<ExternalizedMessageWrapper>();
        Integer budgetYear = sipImportForm.getUniversityFiscalYear();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String principalId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
        
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy ' ' HH:mm:ss", Locale.US);
        
        boolean isValid = validateImportFormData(sipImportForm);
        
        if (!isValid) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        //get start date for log file
        Date startTime = new Date();
        messageList.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_LOG_FILE_HEADER_LINE, dateFormatter.format(startTime)));
        
        //parse file
        if (!sipImportService.importFile(sipImportForm.getFile().getInputStream(), messageList, principalId) ) {
            sipImportService.generateValidationReportInTextFormat(messageList, baos);
            WebUtils.saveMimeOutputStreamAsFile(response, ReportGeneration.TEXT_MIME_TYPE, baos, BCConstants.SIP_IMPORT_LOG_FILE);
            return returnToCaller(mapping, form, request, response);
        }
        
        //messageList.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_COMPLETE, dateFormatter.format(new Date())));
        //Person user = GlobalVariables.getUserSession().getPerson();
        messageList.add(new ExternalizedMessageWrapper("\n\n"));
        messageList.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_LOG_FILE_FOOTER, dateFormatter.format(new Date())));
        
        //write messages to log file
        sipImportService.generateValidationReportInTextFormat(messageList, baos);
        WebUtils.saveMimeOutputStreamAsFile(response, ReportGeneration.TEXT_MIME_TYPE, baos, BCConstants.SIP_IMPORT_LOG_FILE);
        
        return returnToCaller(mapping, form, request, response);
    }
    
    
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * Performs form validation
     * 
     * @param form
     * @return
     */
    public boolean validateImportFormData(SipImportForm form) {
        boolean isValid = true;
        SipImportForm importForm = (SipImportForm) form;
        MessageMap errorMap = GlobalVariables.getMessageMap();
        
        FiscalYearFunctionControlService fiscalYearFunctionControlService = SpringContext.getBean(FiscalYearFunctionControlService.class);
        boolean budgetUpdatesAllowed = fiscalYearFunctionControlService.isBudgetUpdateAllowed(form.getUniversityFiscalYear());
        
        if ( importForm.getFile() == null || importForm.getFile().getFileSize() == 0 ) {
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, BCKeyConstants.ERROR_FILE_IS_REQUIRED);
            isValid = false;
        }
        if ( importForm.getFile() != null && importForm.getFile().getFileSize() == 0 ) {
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, BCKeyConstants.ERROR_FILE_EMPTY);
            isValid = false;
        }
        if (importForm.getFile() != null && (StringUtils.isBlank(importForm.getFile().getFileName())) ) {
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, BCKeyConstants.ERROR_FILENAME_REQUIRED);
            isValid = false;
        }
        if ( !budgetUpdatesAllowed ) {
            errorMap.putError(KFSConstants.GLOBAL_ERRORS, BCKeyConstants.ERROR_PAYRATE_IMPORT_UPDATE_NOT_ALLOWED);
            isValid = false;
        }
        
        
        return isValid;
    }
    
	@Override
	public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    return returnToCaller(mapping, form, request, response);
	}

}

