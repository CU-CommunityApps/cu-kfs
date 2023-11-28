package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.validation.impl.AccountsPayableBankCodeValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.document.validation.impl.CuBankCodeValidation;

public class CuAccountsPayableBankCodeValidation extends AccountsPayableBankCodeValidation {
    
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        final AccountsPayableDocumentBase apDocument = (AccountsPayableDocumentBase) getAccountingDocumentForValidation();

        if (!isDocumentTypeUsingBankCode(apDocument)) {
            return true;
        }

        final String paymentMethodCode = getPaymentMethodCodeFromDocumentIfSupported(apDocument);
        final boolean isValid = CuBankCodeValidation.validate(
                apDocument.getBankCode(), KFSPropertyConstants.DOCUMENT + KFSConstants.DELIMITER + PurapPropertyConstants.BANK_CODE,
                paymentMethodCode, false, true);

        return isValid;
    }

    // This method is private on the superclass, so it has been copied into this class and tweaked accordingly.
    protected boolean isDocumentTypeUsingBankCode(final AccountsPayableDocumentBase apDocument) {
        final String documentTypeName = apDocument.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
        final ParameterEvaluator evaluator = getParameterEvaluatorService().getParameterEvaluator(
                Bank.class, KFSParameterKeyConstants.BANK_CODE_DOCUMENT_TYPES, documentTypeName);
        return evaluator.evaluationSucceeds();
    }

    protected String getPaymentMethodCodeFromDocumentIfSupported(final AccountsPayableDocumentBase apDocument) {
        if (apDocument instanceof PaymentRequestDocument) {
            return ((CuPaymentRequestDocument) apDocument).getPaymentMethodCode();
        } else if (apDocument instanceof VendorCreditMemoDocument) {
            return ((CuVendorCreditMemoDocument) apDocument).getPaymentMethodCode();
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

}
