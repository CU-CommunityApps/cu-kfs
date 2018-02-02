package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class TotalAmountBelowThresholdValidation extends GenericValidation {

    private AccountingDocument accountingDocumentForValidation;
    private ParameterEvaluatorService parameterEvaluatorService;
    private KualiDecimal maximumTotalAmountThresholdAllowed;

    public boolean validate(AttributedDocumentEvent event) {
        KualiDecimal sourceTotalAmount = accountingDocumentForValidation.getSourceTotal();
        KualiDecimal targetTotalAmount = accountingDocumentForValidation.getTargetTotal();
        boolean validationPassed = true;

        if(sourceTotalAmount.compareTo(getMaximumTotalAmountThresholdAllowed()) > 0){
            setGlobalThresholdError(sourceTotalAmount);
            validationPassed = false;
        }

        if(targetTotalAmount.compareTo(getMaximumTotalAmountThresholdAllowed()) > 0){
            setGlobalThresholdError(targetTotalAmount);
            validationPassed = false;
        }

        return validationPassed;
    }

    private void setGlobalThresholdError(KualiDecimal totalAmount){
        GlobalVariables.getMessageMap().putError(
                KFSConstants.AMOUNT_PROPERTY_NAME,
                CUKFSKeyConstants.ERROR_MAX_TOTAL_THRESHOLD_AMOUNT_EXCEEDED,
                totalAmount.toString(),
                getMaximumTotalAmountThresholdAllowed().toString()
        );
    }

    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    public ParameterEvaluatorService getParameterEvaluatorService(){
        if(ObjectUtils.isNull(parameterEvaluatorService)){
            setParameterEvaluatorService(SpringContext.getBean(ParameterEvaluatorService.class));
        }
        return parameterEvaluatorService;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService){
        this.parameterEvaluatorService = parameterEvaluatorService;
    }

    public KualiDecimal getMaximumTotalAmountThresholdAllowed() {
        if(ObjectUtils.isNull(maximumTotalAmountThresholdAllowed)){
            ParameterEvaluator parameterEvaluator = getParameterEvaluatorService().getParameterEvaluator(
                    KFSConstants.CoreModuleNamespaces.FINANCIAL,
                    CuFPParameterConstants.INTERNAL_BILLING_COMPONENT,
                    CuFPParameterConstants.MAX_TOTAL_THRESHOLD_AMOUNT
            );
            setMaximumTotalAmountThresholdAllowed(new KualiDecimal(parameterEvaluator.getValue()));
        }
        return maximumTotalAmountThresholdAllowed;
    }

    public void setMaximumTotalAmountThresholdAllowed(KualiDecimal maximumTotalAmountThresholdAllowed) {
        this.maximumTotalAmountThresholdAllowed = maximumTotalAmountThresholdAllowed;
    }
}
