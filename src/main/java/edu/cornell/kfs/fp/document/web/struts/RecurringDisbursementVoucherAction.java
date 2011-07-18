package edu.cornell.kfs.fp.document.web.struts;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.krad.util.UrlFactory;

import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherPDPStatus;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class RecurringDisbursementVoucherAction extends CuDisbursementVoucherAction implements Serializable {

    private static final long serialVersionUID = -6417266354906825200L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecurringDisbursementVoucherAction.class);
    private static final String dot = ".";
    
    private transient RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService;
    private transient DocumentService documentService;
    
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
        if (getDocumentService().getByDocumentHeaderId(recurringForm.getDocument().getDocumentNumber()) == null) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(dot + "Save", 
                    KFSKeyConstants.ERROR_CUSTOM, "This Document needs to be saved before Submit");
        }
        ActionForward forward = super.route(mapping, form, request, response);
        return forward;
    }
    
    public ActionForward confirmAndCancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RecurringDisbursementVoucherForm recurringForm = (RecurringDisbursementVoucherForm) form;
        RecurringDisbursementVoucherDocument recurringDV = (RecurringDisbursementVoucherDocument) recurringForm.getDocument();
        
        if (StringUtils.isEmpty(recurringDV.getPaymentCancelReason())) {
            String paymentCancelFieldName = AccountingDocumentRuleBaseConstants.ERROR_PATH.DOCUMENT_ERROR_PREFIX + 
                    CUKFSPropertyConstants.RECURRING_DV_PAYMENT_CANCEL_REASON_FIELD_NAME;
            GlobalVariables.getMessageMap().putError(paymentCancelFieldName, CUKFSKeyConstants.ERROR_RCDV_PAYMENT_CANCEL_REASON_REQUIRED);
        } else {
            Set<String> canceledPaymentGroups = getRecurringDisbursementVoucherDocumentService().cancelPDPPayments(recurringDV, recurringDV.getPaymentCancelReason());
            if (canceledPaymentGroups == null || canceledPaymentGroups.isEmpty()) {
                KNSGlobalVariables.getMessageList().add(CUKFSKeyConstants.INFO_RCDV_NO_PAYMENTS_CANCELED);
            } else {
                KNSGlobalVariables.getMessageList().add(CUKFSKeyConstants.INFO_RCDV_LIST_OF_PAYMENT_GROUPS_CANCELED, 
                        buildCommaSpacedList(canceledPaymentGroups));
            }
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }
    
    private String buildCommaSpacedList(Set<String> canceledPaymentGroups) {
        StringBuffer message = new StringBuffer();
        int counter = 0;
        for (String group : canceledPaymentGroups) {
            if (counter > 0) {
                message.append(", ");
            }
            message.append(group);
            counter++;
        }
        return message.toString();
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
