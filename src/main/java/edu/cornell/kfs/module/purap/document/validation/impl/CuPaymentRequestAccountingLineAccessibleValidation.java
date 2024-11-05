package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PaymentRequestAccountingLineAccessibleValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

public class CuPaymentRequestAccountingLineAccessibleValidation extends PaymentRequestAccountingLineAccessibleValidation {
    
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        final PaymentRequestDocument preq = (PaymentRequestDocument)accountingDocumentForValidation;
        if(StringUtils.equals(PaymentRequestStatuses.APPDOC_AWAITING_PAYMENT_METHOD_REVIEW, preq.getApplicationDocumentStatus())) {
            return true;
        }
        else{
            return super.validate(event);
        }
    }

}
