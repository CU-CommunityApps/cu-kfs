package edu.cornell.kfs.module.purap.document;

import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.rice.krad.service.SequenceAccessorService;

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
}
