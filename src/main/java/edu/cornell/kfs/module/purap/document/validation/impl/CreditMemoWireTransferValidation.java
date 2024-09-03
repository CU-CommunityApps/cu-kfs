package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.sys.PaymentMethodAdditionalDocumentData;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.document.validation.impl.PaymentSourceWireTransferValidation;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.businessobject.PaymentMethodExtendedAttribute;

public class CreditMemoWireTransferValidation extends PaymentSourceWireTransferValidation {
    @Override
    protected boolean shouldFieldsBeRequired() {
        final CuVendorCreditMemoDocument cmDoc = (CuVendorCreditMemoDocument) getAccountingDocumentForValidation();
        if (cmDoc.getPaymentMethod() != null) {
            final PaymentMethod cmPaymentMethod = (PaymentMethod) cmDoc.getPaymentMethod();
            final PaymentMethodExtendedAttribute cmPaymentMethodExtension = (PaymentMethodExtendedAttribute) cmPaymentMethod.getExtension();
            return cmPaymentMethodExtension != null
                   && PaymentMethodAdditionalDocumentData.REQUIRED.getCode().equals(
                           cmPaymentMethodExtension.getAdditionalCreditMemoDataCode());
        }
        return false;
    }
}
