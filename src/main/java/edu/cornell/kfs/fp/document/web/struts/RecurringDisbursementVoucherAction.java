package edu.cornell.kfs.fp.document.web.struts;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.sys.CUKFSConstants;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;

public class RecurringDisbursementVoucherAction extends CuDisbursementVoucherAction implements Serializable {

    private static final long serialVersionUID = -6417266354906825200L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecurringDisbursementVoucherAction.class);
    
    private transient RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService;
    private transient DocumentService documentService;
    
    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward actionForward = super.save(mapping, form, request, response);
        openRecurringDetailsTab(form);
        return actionForward;
    }

    private void openRecurringDetailsTab(ActionForm form) {
        RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
        RecurringDisbursementVoucherDocument recurringDV = (RecurringDisbursementVoucherDocument) recurringForm.getDocument();
        if (!recurringDV.getRecurringDisbursementVoucherDetails().isEmpty()) {
            String recurringDetailsTabKey = CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DETAILS_TAB_NAME + 
                    recurringDV.getRecurringDisbursementVoucherDetails().size();
            recurringForm.getTabStates().put(recurringDetailsTabKey, CuFPConstants.OPEN);
        }
    }
    
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
        if (getDocumentService().getByDocumentHeaderId(recurringForm.getDocument().getDocumentNumber()) == null) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(CUKFSConstants.DELIMITER + "Save",
                    KFSKeyConstants.ERROR_CUSTOM, "This Document needs to be saved before Submit");
        }
        ActionForward forward = super.route(mapping, form, request, response);
        return forward;
    }
    
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
    	recurringForm.getPdpStatuses().clear();
    	ActionForward forward = super.copy(mapping, form, request, response);
        return forward;
    }
    
    public RecurringDisbursementVoucherDocumentService getRecurringDisbursementVoucherDocumentService() {
        if (recurringDisbursementVoucherDocumentService == null) {
            recurringDisbursementVoucherDocumentService = SpringContext.getBean(RecurringDisbursementVoucherDocumentService.class);
        }
        return recurringDisbursementVoucherDocumentService;
    }

    public void setRecurringDisbursementVoucherDocumentService(
            RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService) {
        this.recurringDisbursementVoucherDocumentService = recurringDisbursementVoucherDocumentService;
    }
    
    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }


}
