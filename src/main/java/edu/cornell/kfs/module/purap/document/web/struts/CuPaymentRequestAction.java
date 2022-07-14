//UPGRADE-911 commenting out wire stuff
package edu.cornell.kfs.module.purap.document.web.struts;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
//import org.kuali.kfs.fp.businessobject.WireCharge;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedPreCalculateAccountsPayableEvent;
import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestAction;
import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestForm;
import org.kuali.kfs.module.purap.service.PurapAccountRevisionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;

public class CuPaymentRequestAction extends PaymentRequestAction {

	@Override
	public ActionForward docHandler(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ActionForward forward =  super.docHandler(mapping, form, request, response);
        PaymentRequestForm preqForm = (PaymentRequestForm) form;
        PaymentRequestDocument document = (PaymentRequestDocument) preqForm.getDocument();

		if (CollectionUtils.isNotEmpty(document.getItems())) {
			Collections.sort(document.getItems(), new Comparator() {
                public int compare(Object o1, Object o2) {                   
                    PaymentRequestItem item1 = (PaymentRequestItem) o1;
                    PaymentRequestItem item2 = (PaymentRequestItem) o2;
                    Integer inv1 = ((CuPaymentRequestItemExtension)item1.getExtension()).getInvLineNumber();
                    Integer inv2 = ((CuPaymentRequestItemExtension)item2.getExtension()).getInvLineNumber();
                    if (inv1 == null) {
                    	if (inv2 == null) {
                    		return -1;
                    	} else {
                    		return 1;
                    	}
                    } else {
                    	if (inv2 == null) {
                    		return -1;
                    	} else {
                    		return inv1.compareTo(inv2);
                    	}
                    }
                }
            });

	    }
        return forward;
    }
	
	@Override
	protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) {
		super.createDocument(kualiDocumentFormBase);
        // set wire charge message in form
        ((CuPaymentRequestForm) kualiDocumentFormBase).setWireChargeMessage(retrieveWireChargeMessage());
	}
	
    protected String retrieveWireChargeMessage() {

        String message = "";/*SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSKeyConstants.MESSAGE_DV_WIRE_CHARGE);
        WireCharge wireCharge = new WireCharge();
        wireCharge.setUniversityFiscalYear(SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear());

        wireCharge = (WireCharge) SpringContext.getBean(BusinessObjectService.class).retrieve(wireCharge);
        Object[] args = { wireCharge.getDomesticChargeAmt(), wireCharge.getForeignChargeAmt() };*/

        return MessageFormat.format(message, "");
    }
    
    @Override
    protected void customCalculate(PurchasingAccountsPayableDocument apDoc) {
    	super.customCalculate(apDoc);
    	PaymentRequestDocument preqDoc = (PaymentRequestDocument) apDoc;
        // KFSPTS-2578
        if (PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW.equalsIgnoreCase(preqDoc.getApplicationDocumentStatus())
        		&& StringUtils.isNotBlank(preqDoc.getTaxClassificationCode()) && !StringUtils.equalsIgnoreCase(preqDoc.getTaxClassificationCode(), "N")) {
            SpringContext.getBean(PaymentRequestService.class).calculateTaxArea(preqDoc);
            return;
       }

    }
    
    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentRequestDocument preq = ((PaymentRequestForm)form).getPaymentRequestDocument();

        SpringContext.getBean(PurapService.class).prorateForTradeInAndFullOrderDiscount(preq);
        // if tax is required but not yet calculated, return and prompt user to calculate
        if (requiresCalculateTax((PaymentRequestForm)form)) {
            GlobalVariables.getMessageMap().putError(KFSConstants.DOCUMENT_ERRORS, PurapKeyConstants.ERROR_APPROVE_REQUIRES_CALCULATE);
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        // enforce calculating tax again upon approval, just in case user changes tax data without calculation
        // other wise there will be a loophole, because the taxCalculated indicator is already set upon first calculation
        // and thus system wouldn't know it's not re-calculated after tax data are changed
        if (SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedPreCalculateAccountsPayableEvent(preq))) {
            
            ActionForward forward =  super.approve(mapping, form, request, response);
            // need to wait after new item generated itemid
            // preqacctrevision is saved separately
            // TODO : this preqacctrevision is new.  need to validate with existing system to see if '0' is normal ?
            if (StringUtils.equals(preq.getApplicationDocumentStatus(), PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW) || StringUtils.equals(preq.getApplicationDocumentStatus(), PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW)) {
              SpringContext.getBean(PurapAccountRevisionService.class).savePaymentRequestAccountRevisions(preq.getItems(), preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
            }
            return forward;
        }
        else {
            // pre-calculation rules fail, go back to same page with error messages
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
    }
    
    /**
     * Calls service to clear tax info.
     */
    public ActionForward clearTaxInfo(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentRequestForm prForm = (PaymentRequestForm) form;
        PaymentRequestDocument document = (PaymentRequestDocument) prForm.getDocument();

        PaymentRequestService taxService = SpringContext.getBean(PaymentRequestService.class);

        /* call service to clear previous lines */
        taxService.clearTax(document);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
}
