package edu.cornell.kfs.module.ar.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.validation.impl.TotalAmountBilledToDateExceedsAwardTotalSuspensionCategory;

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.module.cg.businessobject.CuAward;

public class CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory extends TotalAmountBilledToDateExceedsAwardTotalSuspensionCategory {
    private static final Logger LOG = LogManager.getLogger(CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory.class);
    
    protected transient ParameterService parameterService;
    
    @Override
    public boolean shouldSuspend(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (shouldValidateOnBudgetTotal()) {
            LOG.debug("shouldSuspend, validating based on award budget total");
            CuAward cuAward = (CuAward) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
            AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) cuAward.getExtension();
            return contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getTotalAmountBilledToDate().isGreaterThan(awardExtension.getBudgetTotalAmount());
        } else {
            LOG.debug("shouldSuspend, validating based on ward total");
            return super.shouldSuspend(contractsGrantsInvoiceDocument);
        }
    }
    
    protected boolean shouldValidateOnBudgetTotal() {
        Boolean validateOnBudgetTotal = parameterService.getParameterValueAsBoolean(ArConstants.AR_NAMESPACE_CODE, ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, 
                CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL, Boolean.TRUE.booleanValue());
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("shouldValidateOnBudgetTotal, the value of " + CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL + " is " + validateOnBudgetTotal.booleanValue());
        }
        
        return validateOnBudgetTotal.booleanValue();
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
