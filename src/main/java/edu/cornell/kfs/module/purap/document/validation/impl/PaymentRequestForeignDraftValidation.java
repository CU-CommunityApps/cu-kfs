package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.PaymentMethodAdditionalDocumentData;
import org.kuali.kfs.sys.document.validation.impl.PaymentSourceForeignDraftValidation;

public class PaymentRequestForeignDraftValidation extends PaymentSourceForeignDraftValidation {

    @Override
    protected boolean documentShouldBeValidated() {
        final PaymentRequestDocument preqDocument = (PaymentRequestDocument) getAccountingDocumentForValidation();
        return preqDocument.getPaymentMethod() == null
               || PaymentMethodAdditionalDocumentData.REQUIRED.getCode().equals(
                       preqDocument.getPaymentMethod().getAdditionalPaymentRequestDataCode());
    }

}