package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.validation.impl.AccountsPayableBankCodeValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedRouteDocumentEvent;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.document.validation.impl.CuBankCodeValidation;

public class CuAccountsPayableBankCodeValidation extends AccountsPayableBankCodeValidation {
    
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        AccountsPayableDocumentBase apDocument = (AccountsPayableDocumentBase) getAccountingDocumentForValidation();

        if (!isDocumentTypeUsingBankCode(apDocument)) {
            return true;
        }

        String paymentMethodCode = getPaymentMethodCodeFromDocumentIfSupported(apDocument);
        boolean isValid = CuBankCodeValidation.validate(
                apDocument.getBankCode(), KFSPropertyConstants.DOCUMENT + KFSConstants.DELIMITER + PurapPropertyConstants.BANK_CODE,
                paymentMethodCode, false, true);

        if (isValid && shouldClearBankCode(apDocument, event, paymentMethodCode)) {
            clearUnneededBankCodeAndWarnUser(
                    apDocument, KFSPropertyConstants.DOCUMENT + KFSConstants.DELIMITER + PurapPropertyConstants.BANK_CODE, paymentMethodCode);
        }

        return isValid;
    }

    // This method is private on the superclass, so it has been copied into this class and tweaked accordingly.
    protected boolean isDocumentTypeUsingBankCode(AccountsPayableDocumentBase apDocument) {
        String documentTypeName = apDocument.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
        ParameterEvaluator evaluator = getParameterEvaluatorService().getParameterEvaluator(
                Bank.class, KFSParameterKeyConstants.BANK_CODE_DOCUMENT_TYPES, documentTypeName);
        return evaluator.evaluationSucceeds();
    }

    protected ParameterEvaluatorService getParameterEvaluatorService() {
        return SpringContext.getBean(ParameterEvaluatorService.class);
    }

    protected String getPaymentMethodCodeFromDocumentIfSupported(AccountsPayableDocumentBase apDocument) {
        if (apDocument instanceof PaymentRequestDocument) {
            return ((CuPaymentRequestDocument) apDocument).getPaymentMethodCode();
        } else if (apDocument instanceof VendorCreditMemoDocument) {
            return ((CuVendorCreditMemoDocument) apDocument).getPaymentMethodCode();
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    protected boolean shouldClearBankCode(
            AccountsPayableDocumentBase apDocument, AttributedDocumentEvent event, String paymentMethodCode) {
        return StringUtils.isNotBlank(apDocument.getBankCode())
                && documentHasPotentialToClearBankCode(apDocument, event)
                && !CuBankCodeValidation.doesBankCodeNeedToBePopulated(paymentMethodCode);
    }

    protected boolean documentHasPotentialToClearBankCode(
            AccountsPayableDocumentBase apDocument, AttributedDocumentEvent event) {
        return (apDocument instanceof PaymentRequestDocument && !(event instanceof AttributedRouteDocumentEvent))
                || apDocument instanceof VendorCreditMemoDocument;
    }

    protected void clearUnneededBankCodeAndWarnUser(
            AccountsPayableDocumentBase apDocument, String bankCodeProperty, String paymentMethodCode) {
        GlobalVariables.getMessageMap().putWarning(bankCodeProperty, CUKFSKeyConstants.WARNING_BANK_NOT_REQUIRED, paymentMethodCode);
        apDocument.setBank(null);
        apDocument.setBankCode(null);
    }

}
