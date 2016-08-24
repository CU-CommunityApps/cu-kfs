package edu.cornell.kfs.fp.document.web.struts;

import java.io.Serializable;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.sys.CUKFSConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

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
    
    public ActionForward confirmAndCancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
        RecurringDisbursementVoucherDocument recurringDV = (RecurringDisbursementVoucherDocument) recurringForm.getDocument();
        
        if (StringUtils.isBlank(recurringDV.getPaymentCancelReason())) {
            String paymentCancelFieldName = AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX + 
                    CUKFSPropertyConstants.RECURRING_DV_PAYMENT_CANCEL_REASON_FIELD_NAME;
            GlobalVariables.getMessageMap().putError(paymentCancelFieldName, CUKFSKeyConstants.ERROR_RCDV_PAYMENT_CANCEL_REASON_REQUIRED);
        } else {
            Set<String> canceledPaymentGroups = getRecurringDisbursementVoucherDocumentService().cancelPDPPayments(recurringDV, recurringDV.getPaymentCancelReason());
            Set<String> canceledDVs = getRecurringDisbursementVoucherDocumentService().cancelDisbursementVouchers(recurringDV, recurringDV.getPaymentCancelReason());
            reportCanceledItemsToBrowser(canceledPaymentGroups, canceledDVs);
            forceRefreshOfPDPStatuses(recurringForm);
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }
    
    private void reportCanceledItemsToBrowser (Set<String> canceledPaymentGroups, Set<String> canceledDVs) {
       if (!canceledPaymentGroups.isEmpty()) {
           KNSGlobalVariables.getMessageList().add(CUKFSKeyConstants.INFO_RCDV_LIST_OF_PAYMENT_GROUPS_CANCELED, StringUtils.join(canceledPaymentGroups, ", "));
       }
       if (!canceledDVs.isEmpty()) {
           KNSGlobalVariables.getMessageList().add(CUKFSKeyConstants.INFO_RCDV_LIST_OF_DVS_CANCELED, StringUtils.join(canceledDVs, ", "));
       }
       if (canceledPaymentGroups.isEmpty() && canceledDVs.isEmpty()) {
           KNSGlobalVariables.getMessageList().add(CUKFSKeyConstants.INFO_RCDV_NO_PAYMENTS_OR_DVS_CANCELED);
       }
    }
    
    private void forceRefreshOfPDPStatuses(RecurringDisbursementVoucherForm recurringForm) {
        recurringForm.setPdpStatuses(null);
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
