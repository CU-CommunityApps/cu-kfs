package edu.cornell.kfs.module.purap.document;

import java.util.List;

import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.rice.krad.service.SequenceAccessorService;

import edu.cornell.kfs.module.purap.CUPurapWorkflowConstants;
import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;

public class CuPaymentRequestDocument extends PaymentRequestDocument {

    public void prepareForSave(KualiDocumentEvent event) {
    	super.prepareForSave(event);
        for (PaymentRequestItem item : (List<PaymentRequestItem>) getItems()) {
            if (item.getItemIdentifier() == null) {
                Integer generatedItemId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber("PMT_RQST_ITM_ID").intValue();
                item.setItemIdentifier(generatedItemId);
            	if (item.getExtension() == null) {
            		item.setExtension(new CuPaymentRequestItemExtension());
            	}

                ((CuPaymentRequestItemExtension)item.getExtension()).setItemIdentifier(generatedItemId);
            }
        }
    }
    
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.REQUIRES_IMAGE_ATTACHMENT)) {
            return requiresAccountsPayableReviewRouting();
        }
        if (nodeName.equals(PurapWorkflowConstants.PURCHASE_WAS_RECEIVED)) {
            return shouldWaitForReceiving();
        }
        if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NON_RESIDENT_ALIEN)) {
            return isVendorEmployeeOrNonResidentAlien();
        }
        //need to stub out for now until 1891 is fully implemented
        if (nodeName.equals(CUPurapWorkflowConstants.TREASURY_MANAGER)) {
        	return false;
        }
        
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName + "\"");
    }
}
